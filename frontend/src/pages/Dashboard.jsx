import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function Dashboard() {
  const navigate = useNavigate();
  const loggedInEmail = localStorage.getItem('userEmail');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  
  // Stări pentru Popup-ul de validare MFA
  const [showPopup, setShowPopup] = useState(false);
  const [mfaCode, setMfaCode] = useState('');
  const [activeProvider, setActiveProvider] = useState('');

  // Dacă nu ești logat, te dă afară la login
  useEffect(() => {
    if (!loggedInEmail) navigate('/login');
  }, [loggedInEmail, navigate]);

  // --- ACȚIUNEA 1: Cerem backend-ului să trimită codul ---
  const handleSetupMfa = async (providerName) => {
    setError('');
    setMessage('Se trimite codul pe email... Te rugăm să aștepți.');
    
    try {
      // Apelăm endpoint-ul de setup creat de tine în AuthController
      const response = await axios.post(`/api/auth/mfa/setup?email=${loggedInEmail}&provider=${providerName}`);
      
      setMessage(response.data.message); // Afișăm "Codul a fost trimis..."
      setActiveProvider(providerName);
      setShowPopup(true); // Deschidem popup-ul
    } catch (err) {
      setError(err.response?.data?.error || 'Eroare la trimiterea codului.');
      setMessage('');
    }
  };

  // --- ACȚIUNEA 2: Trimitem codul completat de user spre validare ---
  const handleConfirmMfa = async () => {
    setError('');
    try {
      const response = await axios.post(`/api/auth/mfa/confirm?email=${loggedInEmail}&provider=${activeProvider}&code=${mfaCode}`);
      
      // Dacă e succes, închidem popup-ul și curățăm input-ul
      setMessage(response.data.message); // Afișăm "MFA activat cu succes!"
      setShowPopup(false);
      setMfaCode('');
      
    } catch (err) {
      setError(err.response?.data?.error || 'Codul introdus este invalid!');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('userEmail');
    navigate('/login');
  };

  return (
    <div className="auth-card" style={{ maxWidth: '500px', position: 'relative' }}>
      <h2 style={{ marginBottom: '5px' }}>Panou de Control</h2>
      <p style={{ textAlign: 'center', color: '#666', marginBottom: '20px' }}>
        Logat ca: <strong>{loggedInEmail}</strong>
      </p>

      {/* Mesaje de stare */}
      {message && <div style={{ color: 'green', textAlign: 'center', marginBottom: '15px', fontWeight: 'bold' }}>{message}</div>}
      {error && <div style={{ color: 'red', textAlign: 'center', marginBottom: '15px', fontWeight: 'bold' }}>{error}</div>}

      <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
        <p style={{ margin: 0, fontWeight: 'bold', color: '#333' }}>Metode de Securitate:</p>
        
        {/* Butonul care declanșează fluxul */}
        <button 
          onClick={() => handleSetupMfa('EMAIL')} 
          className="btn-secondary" 
          style={{ backgroundColor: '#e9f7ef', borderColor: '#28a745', color: '#28a745' }}>
          ✉️ Configurează MFA prin Email
        </button>

        <button onClick={handleLogout} className="btn-primary" style={{ backgroundColor: '#dc3545', marginTop: '20px' }}>
          🚪 Deconectare
        </button>
      </div>

      {/* POP-UP PENTRU INTRODUCEREA CODULUI */}
      {showPopup && (
        <div style={{
          position: 'absolute', top: '10%', left: '5%', right: '5%', 
          backgroundColor: 'white', padding: '20px', borderRadius: '10px', 
          boxShadow: '0 5px 25px rgba(0,0,0,0.5)', zIndex: 10, border: '2px solid #007bff'
        }}>
          <h3 style={{ marginTop: 0, color: '#007bff' }}>Verificare Cod</h3>
          <p style={{ fontSize: '0.9rem', color: '#555' }}>
            Am trimis un cod de 6 cifre la <b>{loggedInEmail}</b>. Te rugăm să-l introduci mai jos:
          </p>
          
          <input 
            type="text" 
            value={mfaCode} 
            onChange={(e) => setMfaCode(e.target.value)} 
            placeholder="Ex: 123456" 
            style={{ 
              width: '90%', marginBottom: '15px', letterSpacing: '5px', 
              textAlign: 'center', fontSize: '1.5rem', fontWeight: 'bold' 
            }}
          />
          
          <div style={{ display: 'flex', gap: '10px' }}>
            <button onClick={handleConfirmMfa} className="btn-primary" style={{ flex: 1 }}>Verifică</button>
            <button onClick={() => setShowPopup(false)} className="btn-secondary">Anulează</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;
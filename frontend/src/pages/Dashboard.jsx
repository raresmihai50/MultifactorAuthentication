import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function Dashboard() {
  const navigate = useNavigate();
  const loggedInEmail = localStorage.getItem('userEmail');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  
  // NOU: Stări pentru Editare Profil
  const [isEditing, setIsEditing] = useState(false);
  const [username, setUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');

  // Stări pentru Popup-ul de validare MFA
  const [showPopup, setShowPopup] = useState(false);
  const [mfaCode, setMfaCode] = useState('');
  const [activeProvider, setActiveProvider] = useState('');

  // Dacă nu ești logat, te dă afară la login
  useEffect(() => {
    if (!loggedInEmail) {
      navigate('/login');
      return;
    }
    
    // Luăm username-ul din backend ca să îl precompletăm în formular
    axios.get(`/api/auth/me?email=${loggedInEmail}`)
      .then(res => setUsername(res.data.username))
      .catch(err => console.log(err));
      
  }, [loggedInEmail, navigate]);

  // --- Funcția de Update Profil ---
  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    
    try {
      const response = await axios.post('/api/auth/update', {
        email: loggedInEmail, // Email-ul rămâne același
        newUsername: username,
        newPassword: newPassword
      });
      setMessage(response.data.message);
      setIsEditing(false); // Închidem formularul
      setNewPassword(''); // Ștergem parola tastată ca să nu rămână pe ecran
    } catch (err) {
      setError(err.response?.data?.error || 'Eroare la actualizare profil.');
    }
  };

  // --- Funcțiile pentru MFA (Același cod ca înainte) ---
  const handleSetupMfa = async (providerName) => {
    setError('');
    setMessage('Se trimite codul pe email... Te rugăm să aștepți.');
    try {
      const response = await axios.post(`/api/auth/mfa/setup?email=${loggedInEmail}&provider=${providerName}`);
      setMessage(response.data.message);
      setActiveProvider(providerName);
      setShowPopup(true);
    } catch (err) {
      setError(err.response?.data?.error || 'Eroare la trimiterea codului.');
      setMessage('');
    }
  };

  const handleConfirmMfa = async () => {
    setError('');
    try {
      const response = await axios.post(`/api/auth/mfa/confirm?email=${loggedInEmail}&provider=${activeProvider}&code=${mfaCode}`);
      setMessage(response.data.message);
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

      {message && <div style={{ color: 'green', textAlign: 'center', marginBottom: '15px', fontWeight: 'bold' }}>{message}</div>}
      {error && <div style={{ color: 'red', textAlign: 'center', marginBottom: '15px', fontWeight: 'bold' }}>{error}</div>}

      {/* Dacă suntem în modul EDITARE afișăm formularul, dacă nu, afișăm meniul normal */}
      {isEditing ? (
        <form onSubmit={handleUpdateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          <div className="form-group">
            <label>Email (Nu poate fi modificat)</label>
            <input type="email" value={loggedInEmail} disabled style={{ backgroundColor: '#f0f0f0', color: '#888' }} />
          </div>
          <div className="form-group">
            <label>Username</label>
            <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Parolă nouă (lasă gol pentru a o păstra pe cea veche)</label>
            <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} placeholder="*******" />
          </div>
          
          <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
            <button type="submit" className="btn-primary" style={{ flex: 1 }}>💾 Salvează</button>
            <button type="button" onClick={() => setIsEditing(false)} className="btn-secondary">Anulează</button>
          </div>
        </form>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          
          <button onClick={() => setIsEditing(true)} className="btn-secondary">
            ✏️ Editează Profilul
          </button>
          
          <hr style={{ width: '100%', border: '0.5px solid #eee' }} />

          <p style={{ margin: 0, fontWeight: 'bold', color: '#333' }}>Metode de Securitate:</p>
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
      )}

      {/* POP-UP PENTRU INTRODUCEREA CODULUI MFA (A rămas la fel) */}
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
            type="text" value={mfaCode} onChange={(e) => setMfaCode(e.target.value)} 
            placeholder="Ex: 123456" 
            style={{ width: '90%', marginBottom: '15px', letterSpacing: '5px', textAlign: 'center', fontSize: '1.5rem', fontWeight: 'bold' }}
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
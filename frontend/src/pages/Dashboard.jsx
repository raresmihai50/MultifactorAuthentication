import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function Dashboard() {
  const navigate = useNavigate();
  const loggedInEmail = localStorage.getItem('userEmail');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  
  const [isEditing, setIsEditing] = useState(false);
  const [username, setUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const [showPopup, setShowPopup] = useState(false);
  const [mfaCode, setMfaCode] = useState('');
  const [activeProvider, setActiveProvider] = useState('');
  
  // NOU: Am adăugat starea pentru a salva imaginea QR de la Backend
  const [qrCodeImage, setQrCodeImage] = useState('');

  useEffect(() => {
    if (!loggedInEmail) {
      navigate('/login');
      return;
    }
    
    axios.get(`/api/auth/me?email=${loggedInEmail}`)
      .then(res => setUsername(res.data.username))
      .catch(err => console.log(err));
      
  }, [loggedInEmail, navigate]);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    
    try {
      const response = await axios.post('/api/auth/update', {
        email: loggedInEmail,
        newUsername: username,
        newPassword: newPassword
      });
      setMessage(response.data.message);
      setIsEditing(false);
      setNewPassword('');
    } catch (err) {
      setError(err.response?.data?.error || 'Eroare la actualizare profil.');
    }
  };

  const handleSetupMfa = async (providerName) => {
    setError('');
    setMessage('Se procesează... Te rugăm să aștepți.');
    setQrCodeImage(''); // Curățăm imaginea la fiecare cerere nouă

    try {
      const response = await axios.post(`/api/auth/mfa/setup?email=${loggedInEmail}&provider=${providerName}`);
      const challengeData = response.data.message; 

      if (challengeData.type === 'QR') {
        setQrCodeImage(challengeData.qrCodeData); // Salvăm imaginea generată de server
        setMessage(challengeData.message); 
      } else {
        setMessage(challengeData.message); 
      }

      setActiveProvider(providerName);
      setShowPopup(true);
    } catch (err) {
      setError(err.response?.data?.error || 'Eroare la procesare.');
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
      setQrCodeImage(''); // Curățăm imaginea după succes
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

          {/* Butonul pentru TOTP care apelează corect funcția */}
          <button 
            onClick={() => handleSetupMfa('TOTP')} 
            className="btn-secondary" 
            style={{ backgroundColor: '#eef5ff', borderColor: '#007bff', color: '#007bff', marginTop: '10px' }}>
            📱 Configurează Google Authenticator
          </button>

          <button onClick={handleLogout} className="btn-primary" style={{ backgroundColor: '#dc3545', marginTop: '20px' }}>
            🚪 Deconectare
          </button>
        </div>
      )}

      {/* POP-UP PENTRU INTRODUCEREA CODULUI MFA */}
      {showPopup && (
        <div style={{
          position: 'absolute', top: '10%', left: '5%', right: '5%', 
          backgroundColor: 'white', padding: '20px', borderRadius: '10px', 
          boxShadow: '0 5px 25px rgba(0,0,0,0.5)', zIndex: 10, border: '2px solid #007bff'
        }}>
          <h3 style={{ marginTop: 0, color: '#007bff' }}>Verificare Cod</h3>
          
          {/* NOU: Textul se schimbă în funcție de metoda aleasă */}
          <p style={{ fontSize: '0.9rem', color: '#555' }}>
            {activeProvider === 'EMAIL' 
              ? <>Am trimis un cod de 6 cifre la <b>{loggedInEmail}</b>. Te rugăm să-l introduci mai jos:</>
              : <>Scanează codul de mai jos în <b>Google Authenticator</b> și introdu codul generat:</>
            }
          </p>
          
          {/* NOU: Aici este magia unde afișăm imaginea cu Codul QR dacă există! */}
          {qrCodeImage && (
            <div style={{ textAlign: 'center', margin: '15px 0' }}>
              <img 
                src={qrCodeImage} 
                alt="QR Code" 
                style={{ width: '200px', border: '1px solid #ddd', borderRadius: '10px' }} 
              />
            </div>
          )}
          
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
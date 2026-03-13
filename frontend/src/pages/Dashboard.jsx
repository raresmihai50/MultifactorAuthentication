import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function Dashboard() {
  const navigate = useNavigate();
  const loggedInEmail = localStorage.getItem('userEmail');

  const [isEditing, setIsEditing] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [selectedMfas, setSelectedMfas] = useState([]);
  const [message, setMessage] = useState('');

  // Verificăm dacă userul e logat când se încarcă pagina
  useEffect(() => {
    if (!loggedInEmail) {
      navigate('/login');
    }
    // TODO: Aici vom trage datele reale ale userului din Backend
    // Momentan punem niște date de test
    setUsername('Nume_Utilizator');
    setSelectedMfas(['TOTP']); 
  }, [loggedInEmail, navigate]);

  const toggleMfa = (methodName) => {
    setSelectedMfas((prev) => 
      prev.includes(methodName) 
        ? prev.filter((m) => m !== methodName) 
        : [...prev, methodName]
    );
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    // TODO: Vom face un apel axios către backend (ex: /api/auth/update)
    console.log("Date noi:", username, password, selectedMfas);
    setMessage("Datele au fost preluate în React! (Urmează să le trimitem la backend)");
    setIsEditing(false);
  };

  const handleLogout = () => {
    localStorage.removeItem('userEmail');
    navigate('/login');
  };

  return (
    <div className="auth-card" style={{ maxWidth: '500px' }}>
      <h2 style={{ marginBottom: '5px' }}>Panou de Control</h2>
      <p style={{ textAlign: 'center', color: '#666', marginBottom: '20px' }}>
        Email: <strong>{loggedInEmail}</strong>
      </p>

      {message && <div style={{ color: 'green', textAlign: 'center', marginBottom: '15px' }}>{message}</div>}

      {!isEditing ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <button onClick={() => setIsEditing(true)} className="btn-secondary">
            ✏️ Editează Profilul și MFA
          </button>
          <button onClick={handleLogout} className="btn-primary" style={{ backgroundColor: '#dc3545' }}>
            🚪 Deconectare
          </button>
        </div>
      ) : (
        <form onSubmit={handleUpdate}>
          <div className="form-group">
            <label>Username Nou</label>
            <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Parolă Nouă (lasă gol pentru a o păstra pe cea veche)</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="***" />
          </div>
          
          <div className="mfa-section">
            <label>Gestionează metodele de securitate (MFA):</label>
            <div className="mfa-buttons">
              <button type="button" className={`mfa-btn ${selectedMfas.includes('TOTP') ? 'active' : ''}`} onClick={() => toggleMfa('TOTP')} style={{ color: 'black' }}>
                📱 Google Auth
              </button>
              <button type="button" className={`mfa-btn ${selectedMfas.includes('EMAIL') ? 'active' : ''}`} onClick={() => toggleMfa('EMAIL')} style={{ color: 'black' }}>
                ✉️ Cod pe Email
              </button>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <button type="submit" className="btn-primary">💾 Salvează Modificările</button>
            <button type="button" onClick={() => setIsEditing(false)} className="btn-secondary" style={{ flex: 1 }}>Anulează</button>
          </div>
        </form>
      )}
    </div>
  );
}

export default Dashboard;
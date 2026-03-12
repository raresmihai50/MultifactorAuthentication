import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios'; // <-- Importăm axios

function Register() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [selectedMfas, setSelectedMfas] = useState([]); 
  
  // Stări noi pentru mesaje de eroare și succes
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const navigate = useNavigate();

  const toggleMfa = (methodName) => {
    setSelectedMfas((prev) => {
      if (prev.includes(methodName)) {
        return prev.filter((m) => m !== methodName);
      } else {
        return [...prev, methodName];
      }
    });
  };

  // Funcția a devenit "async" pentru că așteptăm răspunsul de la server
  const handleRegister = async (e) => {
    e.preventDefault();
    setError(''); // Curățăm erorile vechi
    setSuccess('');

    try {
      // Facem cererea POST către backend (proxy-ul din vite.config.js o va trimite la :8080)
      const response = await axios.post('/api/auth/register', {
        username: username,
        email: email,
        password: password,
        selectedMfas: selectedMfas
      });

      // Dacă a mers bine, afișăm mesajul de la server
      setSuccess(response.data.message);
      
      // Așteptăm 2 secunde, apoi trimitem userul automat la Login
      setTimeout(() => {
        navigate('/login');
      }, 2000);

    } catch (err) {
      // Dacă Spring Boot ne-a dat o eroare (ex: email deja luat), o afișăm
      if (err.response && err.response.data && err.response.data.error) {
        setError(err.response.data.error);
      } else {
        setError('A apărut o eroare la conectarea cu serverul.');
      }
    }
  };

  return (
    <div className="auth-card">
      <h2>Creare Cont Nou</h2>
      
      {/* Afișăm erorile cu roșu */}
      {error && <div style={{ color: 'red', marginBottom: '10px', textAlign: 'center' }}>{error}</div>}
      
      {/* Afișăm succesul cu verde */}
      {success && <div style={{ color: 'green', marginBottom: '10px', textAlign: 'center' }}>{success}</div>}

      <form onSubmit={handleRegister}>
        <div className="form-group">
          <label>Username</label>
          <input 
            type="text" 
            value={username} 
            onChange={(e) => setUsername(e.target.value)} 
            required 
          />
        </div>
        <div className="form-group">
          <label>Email</label>
          <input 
            type="email" 
            value={email} 
            onChange={(e) => setEmail(e.target.value)} 
            required 
          />
        </div>
        <div className="form-group">
          <label>Parolă</label>
          <input 
            type="password" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            required 
          />
        </div>

        <div className="mfa-section">
          <label>Alege metodele de securitate (MFA):</label>
          <div className="mfa-buttons">
            <button 
              type="button" 
              className={`mfa-btn ${selectedMfas.includes('TOTP') ? 'active' : ''}`}
              onClick={() => toggleMfa('TOTP')}
              style={{ color: 'black' }}
            >
              📱 Google Auth
            </button>
            <button 
              type="button" 
              className={`mfa-btn ${selectedMfas.includes('EMAIL') ? 'active' : ''}`}
              onClick={() => toggleMfa('EMAIL')}
              style={{ color: 'black' }}
            >
              ✉️ Cod pe Email
            </button>
          </div>
          <small style={{display: 'block', marginTop: '8px', color: '#666', fontSize: '0.8rem'}}>
            *Poți selecta mai multe metode simultan.
          </small>
        </div>

        <button type="submit" className="btn-primary mt-15">Finalizează Înregistrarea</button>
      </form>

      <div className="auth-footer">
        <p>Ai deja un cont?</p>
        <button onClick={() => navigate('/login')} className="btn-secondary">
          Înapoi la Login
        </button>
      </div>
    </div>
  );
}

export default Register;
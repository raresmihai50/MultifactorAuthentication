import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  
  // Stări pentru Popup-ul de MFA
  const [showMfaPopup, setShowMfaPopup] = useState(false);
  const [mfaCode, setMfaCode] = useState('');
  const [activeProvider, setActiveProvider] = useState('');
  
  // Stare separată pentru erorile din pop-up
  const [mfaError, setMfaError] = useState('');

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setMfaError(''); 

    try {
      const response = await axios.post('/api/auth/login', {
        email: email,
        password: password
      });

      if (response.data.mfaRequired) {
        // Dacă are EMAIL, trimitem codul
        if (response.data.availableMfaMethods.includes('EMAIL')) {
          setActiveProvider('EMAIL');
          setMessage('Parolă corectă! Se trimite codul pe email...');
          
          await axios.post(`/api/auth/login/challenge?email=${email}&provider=EMAIL`);
          
          setMessage(''); 
          setShowMfaPopup(true); 
        } 
        // NOU: Dacă are TOTP, îi deschidem direct Pop-up-ul, fără să mai sunăm la /challenge
        else if (response.data.availableMfaMethods.includes('TOTP')) {
          setActiveProvider('TOTP');
          setMessage(''); // Nu trebuie să așteptăm niciun email
          setShowMfaPopup(true);
        }
      } else {
        localStorage.setItem('userEmail', email);
        navigate('/dashboard');
      }

    } catch (err) {
      setError(err.response?.data?.error || 'Eroare la conectare.');
    }
  };

  const handleVerifyMfa = async () => {
    setMfaError(''); // Curățăm eroarea la fiecare nouă încercare
    try {
      await axios.post(`/api/auth/login/verify?email=${email}&provider=${activeProvider}&code=${mfaCode}`);
      
      localStorage.setItem('userEmail', email);
      setShowMfaPopup(false);
      navigate('/dashboard');
      
    } catch (err) {
      // AICI PUNEM MESAJUL TĂU PERSONALIZAT
      setMfaError('Ai introdus codul greșit! Te rugăm să încerci din nou.');
      setMfaCode(''); // Opțional: îi golim căsuța ca să poată scrie din nou
    }
  };

  return (
    <div className="auth-card" style={{ position: 'relative' }}>
      <h2>Autentificare</h2>
      
      {error && <div style={{ color: 'red', marginBottom: '10px', textAlign: 'center' }}>{error}</div>}
      {message && <div style={{ color: 'green', marginBottom: '10px', textAlign: 'center' }}>{message}</div>}

      <form onSubmit={handleLogin}>
        <div className="form-group">
          <label>Email</label>
          <input 
            type="email" value={email} onChange={(e) => setEmail(e.target.value)} 
            placeholder="Introdu email-ul" required 
          />
        </div>
        <div className="form-group">
          <label>Parolă</label>
          <input 
            type="password" value={password} onChange={(e) => setPassword(e.target.value)} 
            placeholder="Introdu parola" required 
          />
        </div>
        <button type="submit" className="btn-primary">Intră în cont</button>
      </form>
      
      <div className="auth-footer">
        <p>Nu ai un cont încă?</p>
        <button onClick={() => navigate('/register')} className="btn-secondary">
          Înregistrează-te
        </button>
      </div>

      {/* POP-UP PENTRU MFA LA LOGIN */}
      {showMfaPopup && (
        <div style={{
          position: 'absolute', top: '10%', left: '5%', right: '5%', 
          backgroundColor: 'white', padding: '20px', borderRadius: '10px', 
          boxShadow: '0 5px 25px rgba(0,0,0,0.5)', zIndex: 10, border: '2px solid #007bff'
        }}>
          <h3 style={{ marginTop: 0, color: '#007bff' }}>Verificare în 2 Pași</h3>
          <p style={{ fontSize: '0.9rem', color: '#555' }}>
            Am trimis un cod de securitate pe email-ul <b>{email}</b>.
          </p>
          
          {/* Afișăm eroarea direct în Pop-up cu roșu */}
          {mfaError && (
            <div style={{ color: 'red', marginBottom: '15px', textAlign: 'center', fontWeight: 'bold', fontSize: '0.9rem' }}>
              ❌ {mfaError}
            </div>
          )}
          
          <input 
            type="text" 
            value={mfaCode} 
            onChange={(e) => setMfaCode(e.target.value)} 
            placeholder="Ex: 123456" 
            style={{ 
              width: '90%', marginBottom: '15px', letterSpacing: '5px', 
              textAlign: 'center', fontSize: '1.5rem', fontWeight: 'bold',
              borderColor: mfaError ? 'red' : '#ddd' // Facem și marginea căsuței roșie dacă greșește
            }}
          />
          
          <div style={{ display: 'flex', gap: '10px' }}>
            <button onClick={handleVerifyMfa} className="btn-primary" style={{ flex: 1 }}>Login</button>
            <button onClick={() => setShowMfaPopup(false)} className="btn-secondary">Anulează</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Login;
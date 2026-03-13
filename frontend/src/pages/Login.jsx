import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(''); // Pentru afișarea erorilor
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await axios.post('/api/auth/login', {
        email: email,
        password: password
      });

      // Salvăm email-ul în memoria browserului ca să îl folosim pe pagina de profil
      localStorage.setItem('userEmail', email);

      // Verificăm dacă userul are MFA activat
      if (response.data.mfaRequired) {
        // TODO: Aici vom face redirect către pagina de introdus codul MFA!
        // Momentan, doar afișăm o alertă și îl lăsăm să treacă pentru a testa profilul
        alert(`MFA necesar: ${response.data.availableMfaMethods}. Trecem direct la profil pt testare.`);
        navigate('/dashboard');
      } else {
        // Nu are MFA, intră direct
        navigate('/dashboard');
      }

    } catch (err) {
      if (err.response && err.response.data && err.response.data.error) {
        setError(err.response.data.error);
      } else {
        setError('Eroare la conectare.');
      }
    }
  };

  return (
    <div className="auth-card">
      <h2>Autentificare</h2>
      
      {/* Afișarea erorii cu roșu */}
      {error && <div style={{ color: 'red', marginBottom: '10px', textAlign: 'center' }}>{error}</div>}

      <form onSubmit={handleLogin}>
        <div className="form-group">
          <label>Email</label>
          <input 
            type="email" 
            value={email} 
            onChange={(e) => setEmail(e.target.value)} 
            placeholder="Introdu email-ul"
            required 
          />
        </div>
        <div className="form-group">
          <label>Parolă</label>
          <input 
            type="password" 
            value={password} 
            onChange={(e) => setPassword(e.target.value)} 
            placeholder="Introdu parola"
            required 
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
    </div>
  );
}

export default Login;
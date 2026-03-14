import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function Register() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');

    try {
      await axios.post('/api/auth/register', {
        username: username,
        email: email,
        password: password
      });
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.error || 'Eroare la înregistrare');
    }
  };

  return (
    <div className="auth-card">
      <h2>Creare Cont</h2>
      
      {error && <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>}

      <form onSubmit={handleRegister}>
        <div className="form-group">
          <label>Username</label>
          <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Introdu un username" required />
        </div>
        <div className="form-group">
          <label>Email</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Introdu email-ul" required />
        </div>
        <div className="form-group">
          <label>Parolă</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Introdu parola" required />
        </div>
        
        <button type="submit" className="btn-primary">Înregistrează-te</button>
      </form>
      
      <div className="auth-footer">
        <p>Ai deja un cont?</p>
        <button onClick={() => navigate('/login')} className="btn-secondary">
          Loghează-te
        </button>
      </div>
    </div>
  );
}

export default Register;
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault(); // Previne refresh-ul paginii la apăsarea butonului
    console.log("Se încearcă login cu:", email, password);
    // TODO: Aici vom apela backend-ul cu axios când Controller-ul este gata
  };

  return (
    <div className="auth-card">
      <h2>Autentificare</h2>
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
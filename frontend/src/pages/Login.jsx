import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  
  const [showMfaPopup, setShowMfaPopup] = useState(false);
  const [mfaCode, setMfaCode] = useState('');
  const [activeProvider, setActiveProvider] = useState('');
  const [mfaError, setMfaError] = useState('');

  // NOU: O "coadă" cu metodele MFA care mai trebuie trecute
  const [pendingMfaQueue, setPendingMfaQueue] = useState([]);

  const navigate = useNavigate();

  // NOU: Funcție separată care declanșează Pop-up-ul pentru providerul cerut
  const triggerNextMfa = async (provider, userEmail) => {
    setActiveProvider(provider);
    setMfaCode(''); // Curățăm codul vechi
    setMfaError('');
    
    if (provider === 'EMAIL') {
      setMessage('Se trimite codul pe email...');
      try {
        await axios.post(`/api/auth/login/challenge?email=${userEmail}&provider=EMAIL`);
        setMessage(''); 
        setShowMfaPopup(true); 
      } catch (err) {
        setError('Eroare la trimiterea codului pe email.');
      }
    } else if (provider === 'TOTP') {
      setMessage(''); 
      setShowMfaPopup(true);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setMfaError(''); 

    try {
      const response = await axios.post('/api/auth/login', { email, password });

      if (response.data.mfaRequired) {
        const methods = response.data.availableMfaMethods;
        
        // MAGIA AICI: Sortăm metodele ca 'TOTP' să fie mereu primul, apoi restul!
        methods.sort((a, b) => {
          if (a === 'TOTP') return -1;
          if (b === 'TOTP') return 1;
          return 0;
        });
        
        // Extragem prima metodă și restul le punem la "coadă"
        const firstMfa = methods[0];
        setPendingMfaQueue(methods.slice(1)); 
        
        // Lansăm prima provocare (care probabil va fi TOTP)
        await triggerNextMfa(firstMfa, email);

      } else {
        localStorage.setItem('userEmail', email);
        navigate('/dashboard');
      }

    } catch (err) {
      setError(err.response?.data?.error || 'Eroare la conectare.');
    }
  };

  const handleVerifyMfa = async () => {
    setMfaError(''); 
    try {
      await axios.post(`/api/auth/login/verify?email=${email}&provider=${activeProvider}&code=${mfaCode}`);
      
      // Dacă codul este bun, verificăm dacă mai avem metode la rând în coadă!
      if (pendingMfaQueue.length > 0) {
        const nextMfa = pendingMfaQueue[0];
        setPendingMfaQueue(pendingMfaQueue.slice(1)); // Îl scoatem din coadă
        
        setMessage(`Cod ${activeProvider} acceptat! Trecem la pasul următor...`);
        
        // Lansăm următoarea provocare instant!
        await triggerNextMfa(nextMfa, email);
      } else {
        // Dacă coada este goală, am trecut toate testele! Te lăsăm în Dashboard.
        localStorage.setItem('userEmail', email);
        setShowMfaPopup(false);
        navigate('/dashboard');
      }
      
    } catch (err) {
      setMfaError('Ai introdus codul greșit! Te rugăm să încerci din nou.');
      setMfaCode(''); 
    }
  };

  return (
    <div className="auth-card" style={{ position: 'relative' }}>
      <h2>Autentificare</h2>
      
      {error && <div style={{ color: 'red', marginBottom: '10px', textAlign: 'center' }}>{error}</div>}
      {message && <div style={{ color: 'green', marginBottom: '10px', textAlign: 'center', fontWeight: 'bold' }}>{message}</div>}

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

      {/* POP-UP PENTRU MFA */}
      {showMfaPopup && (
        <div style={{
          position: 'absolute', top: '10%', left: '5%', right: '5%', 
          backgroundColor: 'white', padding: '20px', borderRadius: '10px', 
          boxShadow: '0 5px 25px rgba(0,0,0,0.5)', zIndex: 10, border: '2px solid #007bff'
        }}>
          <h3 style={{ marginTop: 0, color: '#007bff' }}>
            {/* Dinamic: Afișăm pasul la care ne aflăm */}
            Verificare în {pendingMfaQueue.length + 1} Pași 🔒
          </h3>
          
          <p style={{ fontSize: '0.9rem', color: '#555' }}>
            {activeProvider === 'EMAIL' 
              ? <>Am trimis un cod de securitate pe email-ul <b>{email}</b>.</>
              : <>Deschide aplicația <b>Google Authenticator</b> de pe telefon și introdu codul de 6 cifre.</>
            }
          </p>
          
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
              borderColor: mfaError ? 'red' : '#ddd' 
            }}
          />
          
          <div style={{ display: 'flex', gap: '10px' }}>
            <button onClick={handleVerifyMfa} className="btn-primary" style={{ flex: 1 }}>Verifică</button>
            <button onClick={() => { setShowMfaPopup(false); setPendingMfaQueue([]); }} className="btn-secondary">Anulează</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Login;
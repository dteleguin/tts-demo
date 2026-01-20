import { useState, useRef, useEffect } from 'react';
import { _axios } from '../main';

const HelloButton = () => {
  const [popover, setPopover] = useState({ show: false, content: '', type: '' });
  const buttonRef = useRef(null);

  const handleClick = async () => {
    try {
      console.log('Button clicked!');
      const response = await _axios.get('/hello');
      console.log('Response:', response.data);
      setPopover({ 
        show: true, 
        content: JSON.stringify(response.data, null, 2), 
        type: 'success' 
      });
      setTimeout(() => setPopover({ show: false, content: '', type: '' }), 5000);
    } catch (error) {
      console.error('Error:', error);
      setPopover({ 
        show: true, 
        content: error.message, 
        type: 'error' 
      });
      setTimeout(() => setPopover({ show: false, content: '', type: '' }), 5000);
    }
  };

  return (
    <div style={{ position: 'relative', display: 'inline-block' }}>
      <button 
        ref={buttonRef}
        className="btn btn-primary" 
        onClick={handleClick}
      >
        Click Me
      </button>
      {popover.show && (
        <div 
          className={`popover bs-popover-top show`}
          role="tooltip"
          style={{
            position: 'absolute',
            bottom: '100%',
            left: '50%',
            transform: 'translateX(-50%)',
            marginBottom: '0.5rem',
            maxWidth: '400px',
            zIndex: 1070,
          }}
        >
          <div className="popover-arrow" style={{
            position: 'absolute',
            bottom: '-0.5rem',
            left: '50%',
            transform: 'translateX(-50%)',
          }}></div>
          <div className={`popover-body ${popover.type === 'error' ? 'bg-danger text-white' : 'bg-success text-white'}`}>
            <pre style={{ margin: 0, whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
              {popover.content}
            </pre>
          </div>
        </div>
      )}
    </div>
  );
};

export default HelloButton;

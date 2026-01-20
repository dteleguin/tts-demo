import { _axios } from '../main';
import toast, { Toaster } from 'react-hot-toast';

const HelloButton = () => {
  const handleClick = async () => {
    try {
      console.log('Button clicked!');
      const response = await _axios.get('/hello');
      console.log('Response:', response.data);
      toast(`Success! Response: ${JSON.stringify(response.data)}`);
    } catch (error) {
      console.error('Error:', error);
      alert(`Error: ${error.message}`);
    }
  };

  return (
    <button className="btn btn-primary" onClick={handleClick}>
      Click Me
    </button>
  );
};

export default HelloButton;

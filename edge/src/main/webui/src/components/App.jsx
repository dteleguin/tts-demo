import Welcome from "./Welcome";
import RenderOnAnonymous from "./RenderOnAnonymous";
import RenderOnAuthenticated from "./RenderOnAuthenticated";
import './App.css'
import Menu from './Menu';
import HelloButton from "./HelloButton";
import toast, { Toaster } from 'react-hot-toast';

const App = ({ store }) => (
  <div className="container">
    <div><Toaster/></div>
    <Menu/>
    <RenderOnAuthenticated>
      <HelloButton/>
    </RenderOnAuthenticated>
  </div>
)

export default App

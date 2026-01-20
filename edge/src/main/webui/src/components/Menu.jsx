import UserService from "../services/UserService";
import RenderOnAnonymous from "./RenderOnAnonymous";
import RenderOnAuthenticated from "./RenderOnAuthenticated";

const Menu = () => (
  <nav className="navbar navbar-expand bg-body-tertiary py-3 mb-5">
    <div className="container-fluid">
      <div className="navbar-brand">Hello</div>
      <div className="collapse navbar-collapse">
        <div className="d-flex ms-auto">
          <RenderOnAuthenticated>
            {/* <div className="navbar-text mx-1" title="Authentication Context Class Reference">
              ACR: {UserService.getTokenParsed().acr}
            </div>
            {UserService.getTokenParsed().acr !== 'gold' && (
            <button className="btn btn-sm btn-warning mx-1"
                    onClick={() => UserService.doLogin({ acr: { values: ['gold'], essential: true } })}>
              Step-Up (Gold)
            </button>)} */}
            <div className="navbar-text mx-1">
              Signed in as <b>{UserService.getUsername()}</b>
            </div>
            <button className="btn btn-sm btn-success ms-1" onClick={() => UserService.doLogout()}>
              Logout
            </button>
          </RenderOnAuthenticated>
          <RenderOnAnonymous>
            <button className="btn btn-sm btn-success ms-1" onClick={() => UserService.doLogin()}>Login</button>
          </RenderOnAnonymous>
        </div>
      </div>
    </div>
  </nav>
)

export default Menu

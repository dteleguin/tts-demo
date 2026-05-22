This is a demo environment for the [Keycloak Transaction Tokens](https://github.com/CarrettiPro/keycloak-tts) implementation, built with [Tilt](https://tilt.dev/) and running on local Kubernetes.

## Prerequisites
- JDK ≥ 17
- [Docker](https://www.docker.com/)
- [kubectl](https://kubernetes.io/docs/reference/kubectl/)
- [kind](https://kind.sigs.k8s.io/)
- [Helm](https://helm.sh/)
- [ctlptl](https://github.com/tilt-dev/ctlptl)
- [Tilt](https://tilt.dev/)
- [mkcert](https://mkcert.dev/)
- (optional) [kubectx](https://kubectx.dev/), [stern](https://github.com/stern/stern), [k9s](https://k9scli.io/)

## Build
```
$ git clone --recurse-submodules https://github.com/dteleguin/tts-demo
$ cd tts-demo
$ mvn clean install -DskipTests
```

## Setup

### Local Cluster
```
$ ctlptl apply -f kind-tts.yaml
Creating registry "ctlptl-registry"...
registry.ctlptl.dev/ctlptl-registry created
No kind clusters found.
Creating cluster "tts" ...
 ✓ Ensuring node image (kindest/node:v1.35.0) 🖼
 ✓ Preparing nodes 📦
 ✓ Writing configuration 📜
 ✓ Starting control-plane 🕹️
 ✓ Installing CNI 🔌
 ✓ Installing StorageClass 💾
Set kubectl context to "kind-tts"
You can now use your cluster with:

kubectl cluster-info --context kind-tts

Have a question, bug, or feature request? Let us know! https://kind.sigs.k8s.io/#community 🙂
   Connecting kind to registry ctlptl-registry
Switched to context "kind-tts".
 🔌 Connected cluster kind-tts to registry ctlptl-registry at localhost:5005
 👐 Push images to the cluster like 'docker push localhost:5005/alpine'
cluster.ctlptl.dev/kind-tts created

```

### DNS & Network
This demo requires the following DNS record to be present (`/etc/hosts` on UNIX):
```
127.0.0.1 edge.test auth.test tts.test
```

Once started, the cluster will listen on port 443; please make sure the port is available.

### TLS
```
$ mkcert tts.test auth.test edge.test

Note: the local CA is not installed in the Firefox and/or Chrome/Chromium trust store.
Note: the local CA is not installed in the Java trust store.
Run "mkcert -install" for certificates to be trusted automatically ⚠️

Created a new certificate valid for the following names 📜
 - "tts.test"
 - "auth.test"
 - "edge.test"

The certificate is at "./tts.test+2.pem" and the key at "./tts.test+2-key.pem" ✅

It will expire on 22 August 2028 🗓

```
You can use `mkcert -install` to install the CA certificate into your browser's truststore. If you prefer manual install, you can find the CA certificate in `$HOME/.local/share/mkcert/`.
You can use any other CA software to generate the certificates. In that case, you might need to adjust the TLS section in `Tiltfile` accordingly - by default, it is assumed that `tts.test+2.pem` contain a single certificate with three SANs (`tts.test`, `auth.test`, `edge.test`).

## Running the Demo
```
$ tilt up
Tilt started on http://localhost:10350/
v0.37.3, built 2026-04-30

(space) to open the browser
(s) to stream logs (--stream=true)
(t) to open legacy terminal mode (--legacy=true)
(ctrl-c) to exit
Opening browser: http://localhost:10350/

```
Wait for all resources to become green in Tilt. In your browser, navigate to [https://edge.test](https://edge.test). Log in as `user` with password `user`, then click the "Click me" button. Navigate back to Tilt and watch the logs for the Transaction Token issuance and propagation.

## Cleanup
```
$ ctlptl delete -f kind-tts.yaml

registry.ctlptl.dev/ctlptl-registry deleted
Deleting cluster "tts" ...
Deleted nodes: ["tts-control-plane"]
cluster.ctlptl.dev/kind-tts deleted
```

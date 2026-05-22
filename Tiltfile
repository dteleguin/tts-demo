# Provision a local Kind cluster using ctlptl:
# ctlptl apply -f kind-tts.yaml

update_settings (k8s_upsert_timeout_secs = 60)
allow_k8s_contexts('kind-tts')

# Load extensions
load('ext://helm_resource', 'helm_resource', 'helm_repo')
load('ext://namespace', 'namespace_create')
load('ext://configmap', 'configmap_create', 'configmap_yaml')
load('ext://secret', 'secret_create_tls')

# Create a TLS secret from a local file
CERT = 'tts.test+2'
CERTFILE = CERT + '.pem'
KEYFILE = CERT + '-key.pem'
secret_create_tls('auth.test', CERTFILE, KEYFILE)
secret_create_tls('tts.test', CERTFILE, KEYFILE)
secret_create_tls('edge.test', CERTFILE, KEYFILE)

# Install NGINX Ingress Controller via helm
helm_repo('ingress-nginx', 'https://kubernetes.github.io/ingress-nginx', resource_name='ingress-nginx-repo', labels=['Helm'])
helm_resource('ingress-nginx', 'ingress-nginx/ingress-nginx', namespace='ingress-nginx', flags=['--values', 'ingress-nginx.yaml', '--create-namespace'])

# Install PostgreSQL via helm
helm_repo('bitnami', 'https://charts.bitnami.com/bitnami', resource_name='bitnami-repo', labels=['Helm'])
helm_resource('postgresql', 'bitnami/postgresql', flags=['--values', 'postgresql.yaml'])

# Install SPIRE via helm
helm_repo('spire', 'https://spiffe.github.io/helm-charts-hardened/', resource_name='spire-repo', labels=['Helm'])
helm_resource('spire-crds', 'spire/spire-crds', namespace='spire-server', pod_readiness='ignore', flags=['--create-namespace'])
helm_resource('spire', 'spire/spire', namespace='spire-server', flags=[
    '--set', 'spiffe-oidc-discovery-provider.tls.spire.enabled=false',
    '--set', 'spire-server.defaultJwtSvidTTL=150s',
    '--create-namespace'],
resource_deps=['spire-crds'])

# Keycloak
helm_repo('codecentric', 'https://codecentric.github.io/helm-charts', resource_name='codecentric-repo', labels=['Helm'])

# Keycloak Config CLI
helm_repo('jkroepke', 'https://jkroepke.github.io/helm-charts', resource_name='jkroepke-repo', labels=['Helm'])

# Install Keycloak AS via helm
helm_resource('keycloak-as', 'codecentric/keycloakx', flags=['--values', 'keycloak-as.yaml'], labels=['TTS'], resource_deps=['ingress-nginx', 'postgresql'])

# Initialize external realm
configmap_create('keycloak-realm-as-external', from_file=['as-external.json=./realms/as-external.json'])
helm_resource('keycloak-config-cli-as', 'jkroepke/keycloak-config-cli', flags=[
    '--values', 'keycloak-config-cli.yaml',
    '--set', 'existingConfigMap=keycloak-realm-as-external',
    '--set', 'env.KEYCLOAK_URL=http://keycloak-as-http/auth'
    ],
    pod_readiness='ignore',
    resource_deps=['keycloak-as'], labels=['TTS'])

# Install Keycloak TTS
docker_build('carretti/keycloak-tts-init', 'keycloak-tts', dockerfile='keycloak-tts/src/main/docker/Dockerfile.init')
helm_resource('keycloak-tts', 'codecentric/keycloakx', flags=['--values', 'keycloak-tts.yaml'],
    image_deps=['carretti/keycloak-tts-init'], image_keys=['initContainerImage'], labels=['TTS'], resource_deps=['ingress-nginx', 'postgresql'])

# Initialize TTS realm
configmap_create('keycloak-realm-tts-internal', from_file=['tts-internal.json=./realms/tts-internal.json'])
helm_resource('keycloak-config-cli-tts', 'jkroepke/keycloak-config-cli', flags=[
    '--values', 'keycloak-config-cli.yaml',
    '--set', 'existingConfigMap=keycloak-realm-tts-internal',
    '--set', 'env.KEYCLOAK_URL=http://keycloak-tts-http/auth'
    ],
    pod_readiness='ignore',
    resource_deps=['keycloak-tts'], labels=['TTS'])

docker_build('carretti/edge', 'edge', dockerfile='edge/src/main/docker/Dockerfile.jvm')
docker_build('carretti/service', 'service', dockerfile='service/src/main/docker/Dockerfile.jvm')

k8s_yaml(helm('helm/tts', name='tts'))
k8s_resource('tts-edge', labels=['TTS'], resource_deps=['spire', 'keycloak-tts'])
k8s_resource('tts-service-a', labels=['TTS'], resource_deps=['tts-edge'])
k8s_resource('tts-service-b', labels=['TTS'], resource_deps=['tts-edge'])

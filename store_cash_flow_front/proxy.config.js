const KEYCLOAK_CONFIG = {
    target: 'http://localhost:9092',
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    onProxyReq: (proxyReq, req, res) => {
        console.log(' Requête proxy vers :', proxyReq.protocol + '//' + proxyReq.host + proxyReq.path);
    }
}

const USER_CONFIG = {
    target: 'http://localhost:9091',
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    onProxyReq: (proxyReq, req, res) => {
        console.log(' Requête proxy vers :', proxyReq.protocol + '//' + proxyReq.host + proxyReq.path);
    }
}

const MIN_IO_CONFIG = {
    target: 'http://localhost:9093',
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    onProxyReq: (proxyReq, req, res) => {
        console.log(' Requête proxy vers :', proxyReq.protocol + '//' + proxyReq.host + proxyReq.path);
    }
}

const CASH_FLOW_API_CONFIG = {
    target: 'http://localhost:9090',
    changeOrigin: true,
    secure: false,
    logLevel: 'debug'
}

const PROXY_CONFIG = {
    '/api/v1/**': CASH_FLOW_API_CONFIG,
    '/api/v1/login': KEYCLOAK_CONFIG,
    '/api/v1/user': USER_CONFIG,
    '/api/v1/minIoApi': MIN_IO_CONFIG
};

module.exports = PROXY_CONFIG;

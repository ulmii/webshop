console.log('XAUTH ' + getCookie('X-Auth'))
localStorage.setItem('X-Auth', getCookie('X-Auth'))
window.location.assign("http://localhost:3000/sso")

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.11.1/firebase-app.js";
import { getAuth,GoogleAuthProvider, signInWithPopup} from "https://www.gstatic.com/firebasejs/10.11.1/firebase-auth.js";
import { getAnalytics } from "https://www.gstatic.com/firebasejs/10.11.1/firebase-analytics.js";
import { createUserWithEmailAndPassword } from "https://www.gstatic.com/firebasejs/10.11.1/firebase-auth.js";

const provider = new GoogleAuthProvider();

const firebaseConfig = {
    apiKey: "Your-API",
    authDomain: "agriautomationhub-f3ceb.firebaseapp.com",
    databaseURL: "https://agriautomationhub-f3ceb-default-rtdb.firebaseio.com",
    projectId: "agriautomationhub-f3ceb",
    storageBucket: "agriautomationhub-f3ceb.appspot.com",
    messagingSenderId: "191351033464",
    appId: "1:191351033464:web:178c01bd37aad4b348278d",
    measurementId: "G-K44DDKGKQX"
};

const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);


const auth = getAuth(app);
auth.languageCode="it"
const button=document.querySelector("#create");
button.addEventListener('click',function(event){
  event.preventDefault();
  const email=document.querySelector("#email").value;
const password=document.querySelector("#password").value;

  createUserWithEmailAndPassword(auth, email, password)
  .then((userCredential) => {
    const user = userCredential.user;
    alert("creating account");
    window.location.href='login.html';
  })
  .catch((error) => {
    const errorCode = error.code;
    const errorMessage = error.message;
    alert("Account already exits");
  });

});


const googleLog = document.querySelector("#google");
googleLog.addEventListener("click",function(event){
  alert("hi")
signInWithPopup(auth, provider)
  .then((result) => {
    const credential = GoogleAuthProvider.credentialFromResult(result);
    const user = result.user;
    console.log(user);
    window.location.href="AgriAutomationHub.html"
  
  }).catch((error) => {
  
    const errorCode = error.code;
    const errorMessage = error.message;
  });

});

  // Import the functions you need from the SDKs you need
  import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-app.js";
  import{getAuth, createUserWithEmailAndPassword} from "https://www.gstatic.com/firebasejs/10.12.0/firebase-auth.js";
  // TODO: Add SDKs for Firebase products that you want to use
  // https://firebase.google.com/docs/web/setup#available-libraries

  // Your web app's Firebase configuration
  // For Firebase JS SDK v7.20.0 and later, measurementId is optional
  const firebaseConfig = {
    apiKey: "AIzaSyA-YeMsoObv2ybpy-W6BDJJJHQNhxAksKs",
    authDomain: "agriautomationhub-f3ceb.firebaseapp.com",
    databaseURL: "https://agriautomationhub-f3ceb-default-rtdb.firebaseio.com",
    projectId: "agriautomationhub-f3ceb",
    storageBucket: "agriautomationhub-f3ceb.appspot.com",
    messagingSenderId: "191351033464",
    appId: "1:191351033464:web:b90b6ec5ea4f48e248278d",
    measurementId: "G-DC4PSFERPT"
  };
  

  // Initialize Firebase
  const app = initializeApp(firebaseConfig);
  const analytics = getAnalytics(app);


  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;

  const submit = document.getElementById('submit');
   submit.addEventListener("click", function(event){
    event.preventDefault()
    createUserWithEmailAndPassword(auth, email, password)
  .then((userCredential) => {
    // Signed up 
    const user = userCredential.user;
    alert("Logging you in ....")
    // ...
  })
  .catch((error) => {
    const errorCode = error.code;
    const errorMessage = error.message;
    alert(errorMessage)
    // ..
  });
   })
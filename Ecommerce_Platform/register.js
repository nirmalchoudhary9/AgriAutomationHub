import { initializeApp } from "https://www.gstatic.com/firebasejs/10.11.1/firebase-app.js";
import { getAuth, GoogleAuthProvider, signInWithPopup, createUserWithEmailAndPassword } from "https://www.gstatic.com/firebasejs/10.11.1/firebase-auth.js";
import { getAnalytics } from "https://www.gstatic.com/firebasejs/10.11.1/firebase-analytics.js";

// Initialize Firebase
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

const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);

const auth = getAuth(app);
auth.languageCode = "it"; // Set the language for auth

const provider = new GoogleAuthProvider();

// Event listener for email/password registration
const button = document.querySelector("#create");
button.addEventListener('click', function(event) {
  event.preventDefault(); // Prevent default form submission

  const email = document.querySelector("#email").value;
  const password = document.querySelector("#password").value;

  createUserWithEmailAndPassword(auth, email, password)
    .then((userCredential) => {
      const user = userCredential.user;
      alert("Account Created");
      window.location.href = 'login.html'; // Redirect to login page
    })
    .catch((error) => {
      const errorCode = error.code;
      const errorMessage = error.message;
      alert("Error: " + errorMessage); // Show a more specific error message
    });
});

// Event listener for Google Sign-In
const googleLog = document.querySelector("#google");
googleLog.addEventListener("click", function(event) {
  event.preventDefault(); // Prevent default form submission
  alert("Opening Google Login");

  signInWithPopup(auth, provider)
    .then((result) => {
      const credential = GoogleAuthProvider.credentialFromResult(result);
      const user = result.user;
      console.log(user);
      window.location.href = "demo.html"; // Redirect after successful Google sign-in
    })
    .catch((error) => {
      const errorCode = error.code;
      const errorMessage = error.message;
      alert("Error during Google sign-in: " + errorMessage); // Show a more specific error message
    });
});

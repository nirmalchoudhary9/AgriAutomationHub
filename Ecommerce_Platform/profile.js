import { getAuth, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.11.1/firebase-auth.js";
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.11.1/firebase-app.js";
import { getAnalytics } from "https://www.gstatic.com/firebasejs/10.11.1/firebase-analytics.js";

// Firebase configuration
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

document.addEventListener("DOMContentLoaded", function() {
    const auth = getAuth();
    const dropdownButton = document.querySelector('.dropdown-profile-button');
    const dropdown = document.querySelector('.dropdown-profile');
    const dropdownContent = document.getElementById('.dropdown-content-profile');
    const profileView = document.getElementById('profile-view');
    const logoutButton = document.getElementById('logout');

    if (dropdownButton) {
        // Toggle dropdown on button click
        dropdownButton.addEventListener('click', function(event) {
            event.stopPropagation(); // Prevent event from bubbling up to document
            dropdown.classList.toggle('active');
        });
    }

    // Close dropdown when clicking outside
    document.addEventListener('click', function(event) {
        if (!dropdown.contains(event.target) && dropdown.classList.contains('active')) {
            dropdown.classList.remove('active');
        }
    });

    // Display user profile and handle logout
    onAuthStateChanged(auth, (user) => {
        if (user) {
            const email = user.email;
            const displayName = user.displayName;
            const photoURL = user.photoURL;

            if (document.getElementById('user-email')) {
                document.getElementById('user-email').textContent = email;
            }
            if (document.getElementById('user-name')) {
                document.getElementById('user-name').textContent = displayName || "N/A";
            }
            if (document.getElementById('user-photo')) {
                document.getElementById('user-photo').src = photoURL || "images/login.png";
            }
        } else {
            window.location.href = 'login.html';
        }
    });

    if (logoutButton) {
        logoutButton.addEventListener('click', function(event) {
            event.preventDefault();
            signOut(auth).then(() => {
                window.location.href = 'login.html';
            }).catch((error) => {
                console.error('Sign Out Error', error);
            });
        });
    }
});


const sign_in_btn = document.querySelector("#sign-in-btn");
const sign_up_btn = document.querySelector("#sign-up-btn");
const container = document.querySelector(".container");

sign_up_btn.addEventListener("click", () => {
  container.classList.add("sign-up-mode");
});

sign_in_btn.addEventListener("click", () => {
  container.classList.remove("sign-up-mode");
});

// Registration form
const registrationForm = document.getElementById('registration-form');
const loginForm = document.getElementById('login-form');


registrationForm.addEventListener('submit', registerUser);
loginForm.addEventListener('submit', loginUser);


function registerUser(event) {
  event.preventDefault();

  const firstName = document.getElementById('first-name').value;
  const lastName = document.getElementById('last-name').value;
  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;
  const confirmPassword = document.getElementById('confirm-password').value;
  const role = document.querySelector('input[name="role"]:checked').value;
  const address = document.getElementById('address').value;
  const phoneNumber = document.getElementById('phone-number').value;

  const user = {
    firstName: firstName,
    lastName: lastName,
    email: email,
    password: password,
    passwordConfirmation: confirmPassword,
    role: role,
    address: address,
    phone_number: phoneNumber
  };

fetch('http://localhost:8080/users', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(user)
  })
    .then(response => {
      if (response.status === 200) {
        // Empty response, registration success
        console.log('Registration success');
        // Redirect to sign-in page
        window.location.href = 'index.html#sign-in';
        return;
      }
      if (!response.ok) {
        throw new Error('Registration failed');
      }
      return response.json();
    })
    .then(data => {
      // Handle registration response if necessary
      console.log(data);
    })
    .catch(error => {
      // Handle error
      console.error(error);
    });
}

function loginUser(event) {
  event.preventDefault();

  const email = document.getElementById('login-email').value;
  const password = document.getElementById('login-password').value;

  const credentials = {
    email: email,
    password: password
  };

  fetch('http://localhost:8080/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: "include",
    body: JSON.stringify(credentials)
  })
    .then(response => {
      if (response.status === 200) {
        // Empty response, login success
        console.log('Login success');
        // Redirect to orders page
        window.location.assign("orders.html");
        return;
      }
      if (!response.ok) {
        throw new Error('Login failed');
      }
      return response.json();
    })
    .then(data => {
      // Handle login response if necessary
      console.log(data);
    })
    .catch(error => {
      // Handle error
      console.error(error);
    });
}
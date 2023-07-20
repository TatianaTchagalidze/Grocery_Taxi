function fetchOpenOrders() {
    fetch('http://localhost:8080/couriers/orders/open', {
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch open orders.');
            }
            return response.json();
        })
        .then(data => {
            const tableBody = document.querySelector('#openOrdersTable tbody');
            tableBody.innerHTML = '';

            data.forEach(order => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${order.id}</td>
                    <td>${order.totalAmount}</td>
                    <td>${order.orderState}</td>
                    <td><button onclick="handlePickupOrder(${order.id})">Pickup</button></td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            const tableBody = document.querySelector('#openOrdersTable tbody');
            tableBody.innerHTML = '<tr><td colspan="4" class="alert">Failed to fetch open orders.</td></tr>';
        });
}


function getInProgressOrdersForOrder() {
    // Get the orderId from the session storage
    const pickupResponse = JSON.parse(sessionStorage.getItem('pickupResponse'));
    if (!pickupResponse || !pickupResponse) {
        console.error('Error: Order ID not found in the pickup response.');
        return;
    }


    const orderId = pickupResponse;
    console.log(orderId);
    fetch(`http://localhost:8080/orders/in_progress/${orderId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error: ${response.status} ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Data received from the server:', data);
            displayInProgressOrders(data); // Pass the single order object directly
        })
        .catch(error => {
            console.error(error);
        });
}

// Handle the pickup order form submission
function handlePickupOrder(event) {
    event.preventDefault();
    const orderId = document.querySelector('#orderIdInput').value;

    fetch(`http://localhost:8080/couriers/orders/${orderId}/pickup`, {
        method: 'POST',
        credentials: 'include'
    })
        .then(response => {
            if (response.ok) {
                return response.json(); // Parse the response body as JSON
            } else {
                throw new Error('Failed to pick up order. Please try again.');
            }
        })
        .then(data => {
            // Save the pickup response in the session
            sessionStorage.setItem('pickupResponse', JSON.stringify(data));
            alert('Order picked up successfully.');

            // Store the orderId from the pickup response in a variable
            const orderIdFromPickupResponse = data.id;

            // Fetch "in_progress" orders for the chosen order ID
            getInProgressOrdersForOrder(orderIdFromPickupResponse);
            fetchOpenOrders(); // Refresh open orders table
        })
        .catch(error => console.error('Error:', error));
}

// Display the "in_progress" orders in the table
function displayInProgressOrders(order) {
    const tableBody = document.querySelector('#delivering tbody');
    tableBody.innerHTML = '';

    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${order.id}</td>
        <td>${order.totalAmount}</td>
        <td>${order.orderState}</td>
        <td><button onclick="closeOrder(${order.id})">Close</button></td>
    `;
    tableBody.appendChild(row);
}

document.addEventListener('DOMContentLoaded', function() {
    fetchOpenOrders();

    // Attach event listener to the pickup order form
    const pickupOrderForm = document.querySelector('#pickupOrderForm');
    if (pickupOrderForm) {
        pickupOrderForm.addEventListener('submit', handlePickupOrder);
    }

    // Fetch open orders when the "Refresh" button is clicked
    const refreshButton = document.querySelector('#refreshButton');
    if (refreshButton) {
        refreshButton.addEventListener('click', fetchOpenOrders);
    }
});



// Close an order
function closeOrder(orderId) {
    fetch(`http://localhost:8080/orders/${orderId}/close`, {
        method: 'PUT',
        credentials: 'include'
    })
        .then(response => {
            if (response.ok) {
                alert('Order closed successfully.');
                fetchOpenOrders(); // Refresh open orders table
            } else {
                alert('Failed to close the order. Please try again.');
            }
        })
        .catch(error => console.error('Error:', error));
}

function logout() {
    fetch('http://localhost:8080/logout', {
        method: 'POST', // Change the method back to GET
        credentials: 'include'
    })
        .then(response => {
            if (response.ok) {
                // If the logout was successful, redirect the user to the registration.html page
                window.location.href = 'registration.html';
            } else {
                console.error('Logout failed');
            }
        })
        .catch(error => {
            console.error('Error during logout:', error);
        });
}


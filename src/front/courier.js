// Fetch open orders and populate the table
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
                order.order_state = undefined;
                const row = document.createElement('tr');
                row.innerHTML = `
          <td>${order.id}</td>
          <td><button onclick="pickupOrder(${order.id})">Pickup</button></td>
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

function handlePickupOrder(event) {
    event.preventDefault();
    const orderId = document.querySelector('#orderIdInput').value;

    fetch(`http://localhost:8080/couriers/orders/${orderId}/pickup`, {
        method: 'POST',
        credentials: 'include'
    })
        .then(response => {
            if (response.ok) {
                alert('Order picked up successfully.');
                fetchOpenOrders(); // Refresh open orders table
            } else {
                alert('Failed to pick up order. Please try again.');
            }
        })
        .catch(error => console.error('Error:', error));
}


// Call fetchOpenOrders when the page loads
window.onload = function() {
  fetchOpenOrders();
};

// Attach event listener to the pickup order form
document.querySelector('#pickupOrderForm').addEventListener('submit', handlePickupOrder);

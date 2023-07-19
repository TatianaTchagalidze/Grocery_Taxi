document.addEventListener('DOMContentLoaded', function () {
    const idFromStorage = JSON.parse(sessionStorage.getItem('userId'));
    const userId = idFromStorage;

    // Function to populate the table with order history data
    function populateOrderHistoryTable(orderHistoryData, tableBody) {
        // Always treat the data as an array, even if it contains only one item
        const orders = Array.isArray(orderHistoryData) ? orderHistoryData : [orderHistoryData];

        orders.forEach(order => {
            const row = document.createElement('tr');

            // Create table cells for each order property
            const orderIdCell = document.createElement('td');
            orderIdCell.textContent = order.id;
            row.appendChild(orderIdCell);

            const dateCell = document.createElement('td');
            dateCell.textContent = order.orderState;
            row.appendChild(dateCell);

            const customerCell = document.createElement('td');
            customerCell.textContent = order.userInfo.email;
            row.appendChild(customerCell);

            const totalAmountCell = document.createElement('td');
            totalAmountCell.textContent = order.totalAmount;
            row.appendChild(totalAmountCell);

            // Append the row to the table body
            tableBody.appendChild(row);
        });
    }


    // Fetch "Closed" orders from the server using the retrieved userId
    fetch(`http://localhost:8080/orders/closed/${userId}`, {
        credentials: 'include'
    })
        .then(response => response.json())
        .then(data => {
            const closedTableBody = document.getElementById('closed-order-body');
            populateOrderHistoryTable(data, closedTableBody);
        })
        .catch(error => {
            console.error('Error fetching "Closed" order history:', error);
        });
});

// Handle click event for the "Move to Order Page" button
document.getElementById('move-to-order-page').addEventListener('click', moveToOrderPage);

function moveToOrderPage() {
    sessionStorage.removeItem('orderItems');
    sessionStorage.removeItem('order');
    // Redirect the user to order.js
    window.location.href = 'orders.html';
}
function logout() {
    fetch('http://localhost:8080/logout', {
        method: 'POST', // Change the method back to GET
        credentials: 'include'
    })
        .then(response => {
            if (response.ok) {
                clearSessionStorage();
                sessionStorage.removeItem('role');
                sessionStorage.removeItem('orderItems');
                sessionStorage.removeItem('userId');
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
function clearSessionStorage() {
    sessionStorage.removeItem('order');
}
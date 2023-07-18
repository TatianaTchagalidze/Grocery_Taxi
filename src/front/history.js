document.addEventListener('DOMContentLoaded', function () {
    const orderFromStorage = JSON.parse(sessionStorage.getItem('order'));
    const userId = orderFromStorage.userInfo.id;

    // Fetch "In Progress" orders from the server using the retrieved userId
    fetch(`http://localhost:8080/orders/in_progress/${userId}`, {
        credentials: 'include'
    })
        .then(response => response.json())
        .then(data => {
            // Call a function to populate the table with the retrieved data
            populateOrderHistoryTable(data);
        })
        .catch(error => {
            console.error('Error fetching order history:', error);
        });

    // Fetch "Closed" orders from the server using the retrieved userId
    fetch(`http://localhost:8080/orders/closed/${userId}`, {
        credentials: 'include'
    })
        .then(response => response.json())
        .then(data => {
            // Call a function to populate the table with the retrieved data
            populateOrderHistoryTable(data);
        })
        .catch(error => {
            console.error('Error fetching order history:', error);
        });
});

function populateOrderHistoryTable(orderHistoryData) {
    const orderHistoryBody = document.getElementById('order-history-body');

    // Iterate over each order in the data
    orderHistoryData.forEach(order => {
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
        orderHistoryBody.appendChild(row);
    });
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

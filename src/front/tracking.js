// Function to fetch the order state from the server
async function getOrderStateFromServer(orderId) {
    try {
        const response = await fetch(`http://localhost:8080/orders/${orderId}`);
        const orderState = await response.text();
        return orderState;
    } catch (error) {
        console.error('Failed to retrieve the order state:', error);
        throw error; // Rethrow the error to handle it further if needed
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    try {
        const orderData = JSON.parse(sessionStorage.getItem('order'));
        if (!orderData || !orderData.id) {
            alert('Order ID not found in the session. Please log in.');
            window.location.href = "registration.html";
            // Handle the situation where the order ID is not in the session
            return;
        }

        const orderId = orderData.id;
        const orderState = await getOrderStateFromServer(orderId);
        console.log(orderId);
        console.log(orderState);

        // Check the order status and update the progress bar accordingly
        const progressBar = document.querySelector('.progress-bar .indicator');
        const progressSteps = document.querySelectorAll('.circle');

        if (orderState === 'OPEN') {
            alert('Your order Status  changed from "DRAFT" to "OPEN"');
            // Order Open
            progressSteps[0].classList.add('active');
            progressBar.style.width = '0%';
        } else if (orderState === 'IN_PROGRESS') {
            alert('Your order Status  changed from "OPEN" to "IN PROGRESS"');
            // Order In Progress
            progressSteps[0].classList.add('active');
            progressSteps[1].classList.add('active');
            progressBar.style.width = '70%';
        } else if (orderState === 'CLOSED') {
            alert('Your order Status  changed from "IN PROGRESS" to "Closed"');
            // Order Closed
            progressSteps[0].classList.add('active');
            progressSteps[1].classList.add('active');
            progressSteps[2].classList.add('active');
            progressBar.style.width = '100%';
        }

        const orderHistoryBtn = document.getElementById('order-history-btn');
        orderHistoryBtn.addEventListener('click', () => {
            // Redirect to the order history page
            if(orderState === 'CLOSED') {
                window.location.href = 'history.html';
            } else {
                alert('Order is not  delivered');
            }
        });
    } catch (error) {
        console.error('Failed to retrieve the order state:', error);
        // Handle any errors that might occur during the fetch process
    }
});

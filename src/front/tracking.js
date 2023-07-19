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

const orderHistoryBtn = document.getElementById('order-history-btn');

orderHistoryBtn.addEventListener('click', async () => {
    try {
        const orderData = JSON.parse(sessionStorage.getItem('order'));
        if (!orderData || !orderData.id) {
            alert('Order ID not found in the session. Please log in.');
            // Handle the situation where the order ID is not in the session
            return;
        }

        const orderId = orderData.id;
        const orderState = await getOrderStateFromServer(orderId);
        console.log(orderId);
        console.log(orderState);

        // Check if the order state is "In_Progress"
        if (orderState !== 'CLOSED') {
            alert('You cannot leave this page while the order is not closed.');
        } else {
            // Redirect to the order history page
            window.location.href = 'history.html';
        }
    } catch (error) {
        console.error('Failed to retrieve the order state:', error);
        // Handle any errors that might occur during the fetch process
    }
});
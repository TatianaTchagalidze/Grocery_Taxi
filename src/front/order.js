// Constants
const searchInput = document.getElementById('search-input');
const searchButton = document.getElementById('search-button');
const productContainer = document.getElementById('product-container');
const cartItems = document.getElementById('cart-items');
const checkoutButton = document.getElementById('checkout-button');
let order; // Variable to store the order

// Event listeners
searchButton.addEventListener('click', searchProducts);
checkoutButton.addEventListener('click', checkoutCart);

// Fetch product data and populate the product container

// Declare cart variable and retrieve cart from session storage
let cart = [];
const storedCart = sessionStorage.getItem('cart');
if (storedCart) {
  cart = JSON.parse(storedCart);
}

// Retrieve order from session storage
const storedOrder = sessionStorage.getItem('order');
if (storedOrder) {
  order = JSON.parse(storedOrder);
}

fetchProductData();

function fetchProductData() {
  fetch('http://localhost:8080/products')
      .then(response => response.json())
      .then(data => {
        data.forEach(product => {
          const productElement = createProductElement(product);
          productContainer.appendChild(productElement);
        });
      })
      .catch(error => {
        console.error('Error fetching product data:', error);
      });
}

function createProductElement(product) {
  const productElement = document.createElement('div');
  productElement.classList.add('product');

  const imageElement = document.createElement('img');
  imageElement.src = `images/${product.id}.png`;
  imageElement.alt = product.name;
  productElement.appendChild(imageElement);

  const nameElement = document.createElement('h3');
  nameElement.textContent = product.name;
  productElement.appendChild(nameElement);

  const priceElement = document.createElement('p');
  priceElement.textContent = `Price: $${product.price}`;
  productElement.appendChild(priceElement);

  const descriptionElement = document.createElement('p');
  descriptionElement.textContent = product.description;
  productElement.appendChild(descriptionElement);

  const quantityElement = document.createElement('input');
  quantityElement.type = 'number';
  quantityElement.min = 1;
  quantityElement.value = 1;
  productElement.appendChild(quantityElement);

  const addToCartButton = document.createElement('button');
  addToCartButton.textContent = 'Add to Cart';
  addToCartButton.addEventListener('click', () => {
    const quantity = parseInt(quantityElement.value, 10);
    addToCart(product, quantity);
  });
  productElement.appendChild(addToCartButton);

  return productElement;
}

function searchProducts() {
  const searchQuery = searchInput.value.toLowerCase();
  const productElements = document.getElementsByClassName('product');

  for (const productElement of productElements) {
    const productName = productElement.querySelector('h3').textContent.toLowerCase();
    const productDescription = productElement.querySelector('p').textContent.toLowerCase();

    if (productName.includes(searchQuery) || productDescription.includes(searchQuery)) {
      productElement.style.display = 'block';
    } else {
      productElement.style.display = 'none';
    }
  }
}

function addToCart(product, quantity) {
  const existingCartItemIndex = cart.findIndex(item => item.product.id === product.id);

  if (existingCartItemIndex !== -1) {
    // If the product is already in the cart, update the quantity
    cart[existingCartItemIndex].quantity = quantity;
  } else {
    // If the product is not in the cart, add a new cart item
    const cartItem = {
      product: product,
      quantity: quantity
    };
    cart.push(cartItem);
  }

  // Remove previous cart items from the cart items container
  cartItems.innerHTML = '';

  // Add the updated cart items to the cart items container
  cart.forEach(cartItem => {
    cartItems.appendChild(createCartItemElement(cartItem));
  });

  saveCartToStorage();
}

function saveCartToStorage() {
  sessionStorage.setItem('cart', JSON.stringify(cart));
}

function createCartItemElement(cartItem) {
  const cartItemElement = document.createElement('li');
  cartItemElement.textContent = `${cartItem.product.name} - Quantity: ${cartItem.quantity}`;
  return cartItemElement;
}

function checkoutCart() {
  if (cart.length === 0) {
    console.log('Cart is empty. Cannot create order.');
    return; // Exit the function if the cart is empty
  }

  const cartItemElements = cartItems.getElementsByTagName('li');
  const orderItems = Array.from(cartItemElements).map(cartItemElement => {
    const productName = cartItemElement.textContent.split(' - ')[0];
    const quantity = parseInt(cartItemElement.textContent.split(' - ')[1].replace('Quantity: ', ''), 10);
    const product = getProductByName(productName);
    return {
      productId: product.id,
      quantity: quantity
    };
  });

  // Save the order items to session storage before creating the order
  saveOrderItemsToStorage(orderItems);

  createOrder(orderItems)
      .then(data => {
        order = data; // Store the complete order object
        console.log('Order created:', data);
        saveOrderToStorage(data); // Save the order in session storage
        window.location.href = `cart.html`;
      })
      .catch(error => {
        console.error('Error creating order:', error);
      });
}

function saveOrderItemsToStorage(orderItems) {
  sessionStorage.setItem('orderItems', JSON.stringify(orderItems));
}
function saveOrderToStorage(order) {
  sessionStorage.setItem('order', JSON.stringify(order));
}

function createOrder(orderItems) {
  return fetch('http://localhost:8080/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(orderItems),
    credentials: 'include'
  })
      .then(response => response.json())
      .then(data => {
        order = data; // Store the complete order object
        console.log(order);
        return data;
      })
      .catch(error => {
        console.error('Error creating order:', error);
      });
}

function getProductByName(name) {
  const productElements = document.getElementsByClassName('product');
  for (const productElement of productElements) {
    const productName = productElement.querySelector('h3').textContent;
    if (productName === name) {
      const productId = productElement.querySelector('img').src.split('/').pop().split('.').shift();
      return {
        id: parseInt(productId, 10),
        name: productName
      };
    }
  }
  return null;
}

const ordersLink = document.querySelector('a[href="order"]');
ordersLink.addEventListener('click', redirectToHistoryPage);

function redirectToHistoryPage(event) {
  event.preventDefault();
  window.location.href = 'history.html';
}

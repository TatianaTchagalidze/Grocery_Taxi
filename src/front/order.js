// Constants
const searchInput = document.getElementById('search-input');
const searchButton = document.getElementById('search-button');
const productContainer = document.getElementById('product-container');
const cartItems = document.getElementById('cart-items');
const checkoutButton = document.getElementById('checkout-button');
let orderId; // Variable to store the order ID

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
  const cartItem = {
    product: product,
    quantity: quantity
  };
  cart.push(cartItem);
  cartItems.appendChild(createCartItemElement(cartItem));
  cartItems.scrollTop = cartItems.scrollHeight;

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

  createOrder(orderItems)
    .then(data => {
      orderId = data.id;
      console.log('Order created:', data);
      window.location.href = `cart.html`;
    })
    .catch(error => {
      console.error('Error creating order:', error);
    });
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

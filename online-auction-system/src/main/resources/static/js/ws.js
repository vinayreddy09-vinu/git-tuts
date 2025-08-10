(function() {
  if (typeof auctionId === 'undefined') return;
  const priceEl = document.getElementById('currentPrice');
  const socket = new SockJS('/ws');
  const stomp = Stomp.over(socket);
  stomp.debug = null;
  stomp.connect({}, function() {
    stomp.subscribe('/topic/auction.' + auctionId, function(message) {
      try {
        const data = JSON.parse(message.body);
        if (data.status === 'NEW_BID') {
          priceEl.className = 'alert alert-info';
          priceEl.textContent = 'Highest bid: ' + data.amount + ' by ' + data.bidder;
        } else if (data.status === 'CLOSED_SOLD') {
          priceEl.className = 'alert alert-success';
          priceEl.textContent = 'Auction closed. Winner: ' + data.bidder + ' at ' + data.amount;
        } else if (data.status === 'CLOSED_UNSOLD') {
          priceEl.className = 'alert alert-secondary';
          priceEl.textContent = 'Auction closed. Reserve not met.';
        }
      } catch (e) {
        console.error(e);
      }
    });
  });
})();
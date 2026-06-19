/**
 * GoBus interactive seat map.
 * totalSeats and bookedSeats are injected by Thymeleaf as global JS vars.
 */

(function () {
    const grid = document.getElementById('seatGrid');
    const bookBtn = document.getElementById('bookBtn');
    const seatInput = document.getElementById('seatNumbersInput');
    const selectedDisplay = document.getElementById('selectedSeatDisplay');
    const selectedInfo = document.getElementById('selectedInfo');

    const selectedSeats = new Set();

    function renderSeats() {
        grid.innerHTML = '';

        for (let i = 1; i <= totalSeats; i++) {
            const posInRow = (i - 1) % 4;
            if (posInRow === 2) {
                const aisle = document.createElement('div');
                aisle.className = 'seat-aisle';
                grid.appendChild(aisle);
            }

            const seat = document.createElement('div');
            seat.className = 'seat';
            seat.textContent = i;
            seat.dataset.seat = i;

            if (bookedSeats.includes(i)) {
                seat.classList.add('seat-booked');
                seat.title = `Seat ${i} - Booked`;
            } else {
                seat.classList.add('seat-available');
                seat.title = `Seat ${i} - Available`;
                seat.addEventListener('click', () => toggleSeat(i, seat));
            }

            grid.appendChild(seat);
        }
    }

    function toggleSeat(num, el) {
        if (selectedSeats.has(num)) {
            selectedSeats.delete(num);
            el.classList.remove('seat-selected');
            el.classList.add('seat-available');
        } else {
            selectedSeats.add(num);
            el.classList.remove('seat-available');
            el.classList.add('seat-selected');
        }

        updateSelection();
    }

    function updateSelection() {
        const seats = Array.from(selectedSeats).sort((a, b) => a - b);
        seatInput.value = seats.join(',');
        bookBtn.disabled = seats.length === 0;

        if (seats.length === 0) {
            selectedInfo.style.display = 'none';
            selectedDisplay.textContent = '-';
            bookBtn.innerHTML = 'Book Selected Seats <i class="fa-solid fa-arrow-right"></i>';
            return;
        }

        selectedDisplay.textContent = seats.join(', ');
        selectedInfo.style.display = 'block';
        bookBtn.innerHTML = `Book ${seats.length} Seat${seats.length > 1 ? 's' : ''} <i class="fa-solid fa-arrow-right"></i>`;
    }

    renderSeats();
})();

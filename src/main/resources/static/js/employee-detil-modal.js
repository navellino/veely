document.addEventListener('DOMContentLoaded', function () {
  const modal = document.getElementById('employeeDetailModal');
  if (!modal) return;
  modal.addEventListener('show.bs.modal', function (event) {
    const btn = event.relatedTarget;
    if (!btn) return;
    const first = btn.getAttribute('data-firstname') || '';
    const last = btn.getAttribute('data-lastname') || '';
    const email = btn.getAttribute('data-email') || '';
    const phone = btn.getAttribute('data-phone') || '';
    const mobile = btn.getAttribute('data-mobile') || '';
    const photo = btn.getAttribute('data-photo');

    modal.querySelector('.modal-title').textContent = first + ' ' + last;
    modal.querySelector('#empModalEmail').textContent = email;
    modal.querySelector('#empModalPhone').textContent = phone || '-';
    modal.querySelector('#empModalMobile').textContent = mobile || '-';
    const img = modal.querySelector('#empModalPhoto');
    if (photo) {
      img.src = photo;
      img.classList.remove('d-none');
    } else {
      img.src = '';
      img.classList.add('d-none');
    }
  });
});
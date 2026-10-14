function confirmAction(msg){return window.confirm(msg)}
function filterTable(inputId,tableId){const q=document.getElementById(inputId).value.toLowerCase();document.querySelectorAll('#'+tableId+' tbody tr').forEach(r=>r.style.display=r.innerText.toLowerCase().includes(q)?'':'none')}
async function loadRecommendations(ctx,dutyId){const box=document.getElementById('recommendations');box.innerHTML='<div class="empty">Checking eligibility, conflicts, workload and skills…</div>';try{const r=await fetch(ctx+'/allocation?duty_id='+encodeURIComponent(dutyId));const data=await r.json();if(!data.length){box.innerHTML='<div class="empty">No eligible staff were found for this duty.</div>';return}box.innerHTML=data.map((x,i)=>`<div class="candidate ${i===0?'recommended':''}"><div class="d-flex justify-content-between"><div><b>${escapeHtml(x.name)}</b><div class="mini">${escapeHtml(x.code)} · ${x.hours} h on duty date</div></div><div class="text-end"><div class="score">${x.score}</div><div class="mini">/100 suitability</div></div></div><div class="row g-2 mt-2"><div class="col"><div class="mini">Skill</div><b>${x.skill}</b></div><div class="col"><div class="mini">Workload</div><b>${x.workload}</b></div><div class="col"><div class="mini">Experience</div><b>${x.experience}</b></div><div class="col"><div class="mini">Fairness</div><b>${x.fairness}</b></div></div><div class="meter mt-2"><span style="width:${x.score}%"></span></div><div class="reason">✓ ${x.reasons.join(' · ')}</div><form method="post" action="${ctx}/allocation" class="mt-3"><input type="hidden" name="duty_id" value="${dutyId}"><input type="hidden" name="staff_id" value="${x.staffId}"><button class="btn-main" type="submit">Assign this staff member</button></form></div>`).join('')}catch(e){box.innerHTML='<div class="alert alert-danger">Unable to load recommendations.</div>'}}
function escapeHtml(s){return String(s).replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]))}


// Mobile navigation and responsive table support for AssignPro.
document.addEventListener('DOMContentLoaded', function () {
  const side = document.querySelector('.side');
  const menuButton = document.querySelector('.mobile-nav-toggle');
  if (side && menuButton) {
    menuButton.addEventListener('click', function () {
      const isOpen = side.classList.toggle('mobile-open');
      menuButton.setAttribute('aria-expanded', String(isOpen));
      menuButton.setAttribute('aria-label', isOpen ? 'Close navigation menu' : 'Open navigation menu');
      const icon = menuButton.querySelector('i');
      if (icon) icon.className = isOpen ? 'fa-solid fa-xmark' : 'fa-solid fa-bars';
    });
    side.querySelectorAll('.nav a').forEach(function (link) {
      link.addEventListener('click', function () {
        if (window.matchMedia('(max-width: 900px)').matches) {
          side.classList.remove('mobile-open');
          menuButton.setAttribute('aria-expanded', 'false');
          menuButton.setAttribute('aria-label', 'Open navigation menu');
          const icon = menuButton.querySelector('i');
          if (icon) icon.className = 'fa-solid fa-bars';
        }
      });
    });
  }

  // Any table that isn't already wrapped can scroll horizontally on small screens.
  document.querySelectorAll('table.table').forEach(function (table) {
    if (!table.closest('.table-responsive')) {
      const wrapper = document.createElement('div');
      wrapper.className = 'table-responsive mobile-table-scroll';
      table.parentNode.insertBefore(wrapper, table);
      wrapper.appendChild(table);
    }
  });

  // Avoid accidental double-taps and keep form controls usable on phones.
  document.querySelectorAll('input, select, textarea').forEach(function (control) {
    if (!control.getAttribute('autocomplete') && control.type !== 'file' && control.type !== 'hidden') {
      control.setAttribute('autocomplete', 'on');
    }
  });
});

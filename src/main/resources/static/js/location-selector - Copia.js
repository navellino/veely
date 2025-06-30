// static/js/location-selector.js

class LocationSelector extends HTMLElement {
  constructor() {
    super();
    // 1) UI isolata in Shadow DOM
    this.shadow = this.attachShadow({ mode: 'open' });

    // 2) Contenitori per i dati JSON
    this.nations    = [];
    this.regions    = [];
    this.provinces  = [];
    this.comuni     = [];
    this.comuniCap  = [];  // ← qui finirà sempre un Array

    // 3) Hidden inputs per Spring MVC bind
    this._hiddenInputs = {};
  }

  async connectedCallback() {
    // ───────────────────────────────────────────────
    // ATTRIBUTI DI BINDING: i property-path Thymeleaf
    // es. "residenceAddress.postalCode"
    // (passati letteralmente in form.html, non valutati!)
    this.nameCountry  = this.getAttribute('country-binding')  || '';
    this.nameRegion   = this.getAttribute('name-region')      || '';
    this.nameProvince = this.getAttribute('name-province')    || '';
    this.nameCity     = this.getAttribute('name-city')        || '';
    this.namePostal   = this.getAttribute('name-postal')      || '';

    // ───────────────────────────────────────────────
    // ATTRIBUTI DI PREFILL (solo in edit)
    this.initialCountry  = this.getAttribute('data-selected-country')  || '';
    this.initialRegion   = this.getAttribute('data-selected-region')   || '';
    this.initialProvince = this.getAttribute('data-selected-province') || '';
    this.initialCity     = this.getAttribute('data-selected-city')     || '';
    this.initialPostal   = this.getAttribute('data-selected-postal')   || '';

    // ───────────────────────────────────────────────
    // FETCH PARALLELO DEI 5 JSON
    // NOTA: se uno dei fetch fallisce, l’errore arriverà qui
    let jsonN, jsonR, jsonP, jsonC, jsonCC;
    try {
      [jsonN, jsonR, jsonP, jsonC, jsonCC] = await Promise.all([
        fetch('/js/gi_nazioni.json').then(r => r.json()),
        fetch('/js/gi_regioni.json').then(r => r.json()),
        fetch('/js/gi_province.json').then(r => r.json()),
        fetch('/js/gi_comuni.json').then(r => r.json()),
        fetch('/js/cap.json').then(r => r.json())
      ]);
    } catch (err) {
      console.error('Errore caricamento JSON location-selector:', err);
      return; // non proseguiamo
    }

    // 4) Salvo i dati nei container
    this.nations   = Array.isArray(jsonN) ? jsonN : [];
    this.regions   = Array.isArray(jsonR) ? jsonR : [];
    this.provinces = Array.isArray(jsonP) ? jsonP : [];
    this.comuni    = Array.isArray(jsonC) ? jsonC : [];

    // ───────────────────────────────────────────────
    // NORMALIZZAZIONE DEL JSON dei CAP:
    // - se è già Array → OK
    // - se è un oggetto con chiavi ISTAT → prendo i valori
    // - se è un nested object (es. { data: [...] }) → cerco un array
    let capsCandidate = jsonCC;
    if (!Array.isArray(capsCandidate)) {
      if (capsCandidate && Array.isArray(capsCandidate.comuniCap)) {
        capsCandidate = capsCandidate.comuniCap;
      } else if (capsCandidate && Array.isArray(capsCandidate.data)) {
        capsCandidate = capsCandidate.data;
      } else if (capsCandidate && typeof capsCandidate === 'object') {
        // prova a estrarre tutti i valori, poi filtra array interni
        const vals = Object.values(capsCandidate).flat();
        if (Array.isArray(vals)) capsCandidate = vals;
        else capsCandidate = [];
      } else {
        console.warn('Formato inesperato per cap.json:', capsCandidate);
        capsCandidate = [];
      }
    }
    this.comuniCap = capsCandidate;

    // ───────────────────────────────────────────────
    // DISEGNO UI + hidden inputs + event listener + prefill
    this.render();              // crea i <select> + label
    this._renderHiddenInputs(); // hidden <input type="hidden">
    this.attachListeners();     // onChange handlers
    this._prefill();            // se siamo in edit, imposta i valori
  }

  /** 1) Disegna tutti i <select> + label nel Shadow DOM */
  render() {
    this.shadow.innerHTML = `
      <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
            rel="stylesheet">
      <div class="row gx-3 gy-2 align-items-end">
        <!-- Paese -->
        <div class="col-lg-3">
          <label class="form-label" for="countrySelect">Paese</label>
          <select class="form-select" id="countrySelect">
            <option value="">— seleziona —</option>
            ${this.nations.map(n =>
              `<option value="${n.sigla_nazione}">${n.denominazione_nazione}</option>`
            ).join('')}
          </select>
        </div>
        <!-- Regione -->
        <div class="col-lg-3">
          <label class="form-label" for="regionSelect">Regione</label>
          <select class="form-select" id="regionSelect" disabled>
            <option value="">— seleziona —</option>
          </select>
        </div>
        <!-- Provincia -->
        <div class="col-lg-3">
          <label class="form-label" for="provinceSelect">Provincia</label>
          <select class="form-select" id="provinceSelect" disabled>
            <option value="">— seleziona —</option>
          </select>
        </div>
        <!-- Comune -->
        <div class="col-lg-3">
          <label class="form-label" for="citySelect">Comune</label>
          <select class="form-select" id="citySelect" disabled>
            <option value="">— seleziona —</option>
          </select>
        </div>
        <!-- CAP (ora un SELECT) -->
        <div class="col-lg-2">
          <label class="form-label" for="postalSelect">CAP</label>
          <select class="form-select" id="postalSelect" disabled>
            <option value="">— seleziona —</option>
          </select>
        </div>
      </div>
    `;
  }

  /** 2) Crea nel Light DOM i <input type="hidden"> per Spring MVC */
  _renderHiddenInputs() {
    const container = document.createElement('div');
    container.style.display = 'none';

    [
      ['country',  this.nameCountry ],
      ['region',   this.nameRegion  ],
      ['province', this.nameProvince],
      ['city',     this.nameCity    ],
      ['postal',   this.namePostal  ]
    ].forEach(([key, name]) => {
      if (!name) return;
      const inp = document.createElement('input');
      inp.type  = 'hidden';
      inp.name  = name;   // es. "residenceAddress.postalCode"
      inp.value = '';
      container.appendChild(inp);
      this._hiddenInputs[key] = inp;
    });

    this.appendChild(container);
  }

  /** 3) Associa gli event listener per ogni SELECT */
  attachListeners() {
    this.shadow.getElementById('countrySelect')
        .addEventListener('change', () => this.onCountryChange());
    this.shadow.getElementById('regionSelect')
        .addEventListener('change', () => this.onRegionChange());
    this.shadow.getElementById('provinceSelect')
        .addEventListener('change', () => this.onProvinceChange());
    this.shadow.getElementById('citySelect')
        .addEventListener('change', () => this.onCityChange());
    // Se l’utente cambia manualmente il CAP
    this.shadow.getElementById('postalSelect')
        .addEventListener('change', () => {
          if (this._hiddenInputs.postal) {
            this._hiddenInputs.postal.value =
              this.shadow.getElementById('postalSelect').value;
          }
        });
  }

  /** 4a) Cambio Paese → reset a valle, popola Regioni (solo IT), sync hidden */
  onCountryChange() {
    const cc     = this.shadow.getElementById('countrySelect').value;
    const rSel   = this.shadow.getElementById('regionSelect');
    const pSel   = this.shadow.getElementById('provinceSelect');
    const cSel   = this.shadow.getElementById('citySelect');
    const capSel = this.shadow.getElementById('postalSelect');

    // Reset cascata
    [rSel, pSel, cSel, capSel].forEach(s => {
      s.innerHTML = '<option value="">— seleziona —</option>';
      s.disabled  = true;
    });

    // Sync hidden country
    if (this._hiddenInputs.country) {
      this._hiddenInputs.country.value = cc;
    }

    // Solo se IT popolo le regioni (JSON contiene solo regioni IT)
    if (cc === 'IT') {
      this.regions
        .sort((a,b) => a.denominazione_regione.localeCompare(b.denominazione_regione))
        .forEach(r => rSel.add(new Option(r.denominazione_regione, r.codice_regione)));
      rSel.disabled = false;
    }
  }

  /** 4b) Cambio Regione → reset a valle, popola Province, sync hidden */
  onRegionChange() {
    const code   = this.shadow.getElementById('regionSelect').value;
    const pSel   = this.shadow.getElementById('provinceSelect');
    const cSel   = this.shadow.getElementById('citySelect');
    const capSel = this.shadow.getElementById('postalSelect');

    [pSel, cSel, capSel].forEach(s => {
      s.innerHTML = '<option value="">— seleziona —</option>';
      s.disabled  = true;
    });
    if (this._hiddenInputs.region) {
      this._hiddenInputs.region.value = code;
    }
    if (!code) return;

    this.provinces
      .filter(p => p.codice_regione === code)
      .sort((a,b) => a.denominazione_provincia.localeCompare(b.denominazione_provincia))
      .forEach(p => pSel.add(new Option(p.denominazione_provincia, p.sigla_provincia)));
    pSel.disabled = false;
  }

  /** 4c) Cambio Provincia → reset a valle, popola Comuni, sync hidden */
  onProvinceChange() {
    const sig    = this.shadow.getElementById('provinceSelect').value;
    const cSel   = this.shadow.getElementById('citySelect');
    const capSel = this.shadow.getElementById('postalSelect');

    [cSel, capSel].forEach(s => {
      s.innerHTML = '<option value="">— seleziona —</option>';
      s.disabled  = true;
    });
    if (this._hiddenInputs.province) {
      this._hiddenInputs.province.value = sig;
    }
    if (!sig) return;

    this.comuni
      .filter(c => c.sigla_provincia === sig)
      .sort((a,b) => a.denominazione_ita.localeCompare(b.denominazione_ita))
      .forEach(c => cSel.add(new Option(c.denominazione_ita, c.codice_istat)));
    cSel.disabled = false;
  }

  /**
   * 4d) Cambio Comune → estraggo i CAP da this.comuniCap
   * → popolo il <select> CAP → sync hidden city + postal
   */
  onCityChange() {
    const istat  = this.shadow.getElementById('citySelect').value;
    const capSel = this.shadow.getElementById('postalSelect');

    // Filtro solo i record di CAP per questo ISTAT
    const matches = this.comuniCap.filter(c => c.codice_istat === istat);

    // Estraggo un array unico di CAP, senza duplicati e senza valori vuoti
    const caps = Array.from(new Set(matches.map(c => c.cap).filter(Boolean)));

    // Reset e popolamento del select CAP
    capSel.innerHTML = '<option value="">— seleziona —</option>';
    caps.sort().forEach(cap => capSel.add(new Option(cap, cap)));
    capSel.disabled = caps.length === 0;

    // Se c’è un solo CAP, lo pre-seleziono
    if (caps.length === 1) {
      capSel.value = caps[0];
    }

    // Sync hidden inputs
    if (this._hiddenInputs.city)   this._hiddenInputs.city.value   = istat;
    if (this._hiddenInputs.postal) this._hiddenInputs.postal.value = capSel.value;
  }

  /** 5) Prefill in edit: imposto i valori iniziali e innesco la cascata */
  _prefill() {
    if (this.initialCountry) {
      this.shadow.getElementById('countrySelect').value = this.initialCountry;
      this._hiddenInputs.country.value = this.initialCountry;
      this.onCountryChange();
    }
    if (this.initialRegion) {
      this.shadow.getElementById('regionSelect').value = this.initialRegion;
      this._hiddenInputs.region.value = this.initialRegion;
      this.onRegionChange();
    }
    if (this.initialProvince) {
      this.shadow.getElementById('provinceSelect').value = this.initialProvince;
      this._hiddenInputs.province.value = this.initialProvince;
      this.onProvinceChange();
    }
    if (this.initialCity) {
      this.shadow.getElementById('citySelect').value = this.initialCity;
      this._hiddenInputs.city.value = this.initialCity;
      this.onCityChange();
    }
    if (this.initialPostal) {
      this.shadow.getElementById('postalSelect').value = this.initialPostal;
      this._hiddenInputs.postal.value = this.initialPostal;
    }
  }
}

// Registrazione del WebComponent
customElements.define('location-selector', LocationSelector);
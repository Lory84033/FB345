# Friendbook per Android

App Android (4.1 e successivi) che apre Friendbook a schermo intero, con l'icona della "f".
Il sito è già dentro l'app (cartella `app/src/main/assets/www`), l'accesso resta salvato
e il pulsante "Foto" apre la galleria del telefono.

GitHub compila l'APK da solo: non serve Android Studio.

## Come ottenere l'APK

1. Su github.com premi **+** in alto → **New repository**. Nome: `friendbook-android`. Premi **Create repository**.
2. Nella pagina nuova premi **uploading an existing file**.
3. Apri la cartella estratta dallo zip e **trascina dentro tutti i file e le cartelle** (non lo zip).
   Deve comparire anche la cartella `.github`. Se non compare, vedi "Se manca .github" qui sotto.
4. In fondo premi **Commit changes**.
5. Vai nella scheda **Actions**: parte "Compila APK" e ci mette circa 5 minuti.
   Quando c'è la spunta verde è finita.
6. Torna alla pagina principale del repository e, a destra, apri **Releases** → **Friendbook (ultima versione)**.
7. Tocca **Friendbook.apk** per scaricarlo sul telefono e installalo.
   Se Android lo blocca: Impostazioni → Sicurezza → **Origini sconosciute** (o "Installa app sconosciute" per il browser).

Ogni volta che cambi un file nel repository, GitHub rifà l'APK e aggiorna la stessa pagina Releases.
Le nuove versioni si installano sopra la vecchia senza perdere l'accesso.

### Se manca .github
Premi **Add file** → **Create new file**, come nome scrivi `.github/workflows/build-apk.yml`
e incolla dentro il testo del file `workflow-da-incollare.txt`. Poi **Commit changes**.

### Se la compilazione diventa rossa
Apri la compilazione fallita in Actions, apri il passaggio con la X rossa e copia le ultime righe di errore.

## Cose che puoi cambiare

- **Sito online invece di quello interno:** apri `app/src/main/res/values/config.xml` e scrivi l'indirizzo
  tra `<string name="start_url">` e `</string>`. Se non si apre, l'app usa quello interno.
- **Collegamento a Firebase:** `app/src/main/assets/www/config.js`.
- **Nome dell'app:** `app/src/main/res/values/strings.xml`.

## Limiti

- Su Android 4.1–4.3 il telefono può non riuscire a collegarsi a Firebase (connessioni sicure vecchie): in quel caso il login non funziona.
- Non ci sono notifiche con l'app chiusa e non c'è integrazione con BlinkFeed.
- La chiave di firma (`app/friendbook.keystore`, password `friendbook`) è nel progetto: va bene per uso personale, non per Google Play.

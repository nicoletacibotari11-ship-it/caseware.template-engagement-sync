# template-engagement-sync

Sincronizează engagement-urile cu versiunile de template publicate (System C din
`template-update-pipeline.html`). Acest modul implementează **Engagement Controller**
din Engagement Service: ține evidența versiunii aplicate per engagement și a ultimei
versiuni cunoscute per template, și expune API-ul intern consumat de Outbox Worker și
de Dashboard.

## Running tests

Unit tests:
```
.\mvnw test -Dtest=EngagementServiceTest
```

## Start docker și Postgres

```
cd .\docker\
docker compose up -d
```

Dacă `psql` raportează că baza de date nu există (de exemplu, pentru un container
inițializat anterior), creeaz-o conectându-te la baza implicită `postgres`:

```
docker exec -it docker-postgres-1 psql -U postgres -d postgres -c "CREATE DATABASE template_engagement_sync;"
```

Conectează-te apoi la baza proiectului și verifică tabelele:

```
docker exec -it docker-postgres-1 psql -U postgres -d template_engagement_sync
template_engagement_sync=# \dt
```

Flyway rulează automat migrațiile (schema + seed data) la pornirea aplicației.

## Start Application

Din rădăcina proiectului, după ce Postgres rulează:

```
.\mvnw.cmd spring-boot:run
```

Aplicația pornește pe `http://localhost:8080`.

## API examples

Datele de test (seed) vin cu template-urile `11111111-1111-1111-1111-111111111111`
(versiune 3) și `22222222-2222-2222-2222-222222222222` (versiune 2), și firma
`aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa` cu engagement-uri `current`/`pending`.

**Creează un engagement** (apelat de Outbox Worker la `engagement.created`):
```
curl -X POST http://localhost:8080/internal/v1/engagements/c1111111-1111-1111-1111-111111111111 ^
  -H "Content-Type: application/json" ^
  -d "{\"firmId\":\"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa\",\"templateId\":\"11111111-1111-1111-1111-111111111111\",\"appliedVersion\":2}"
```

**Trimite o decizie Apply/Decline** (apelat de Outbox Worker la `engagement.decided`):
```
curl -X POST http://localhost:8080/internal/v1/engagements/aaaa1111-0000-0000-0000-000000000002/decision ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"APPLY\",\"decidedVersion\":3}"
```

```
curl -X POST http://localhost:8080/internal/v1/engagements/aaaa1111-0000-0000-0000-000000000003/decision ^
  -H "Content-Type: application/json" ^
  -d "{\"decision\":\"DECLINE\"}"
```

**Listează engagement-urile pending ale unei firme** (apelat de Dashboard, paginat):
```
curl "http://localhost:8080/internal/v1/engagements/pending?firmId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa&page=0&size=20"
```

> Comenzile `curl` de mai sus folosesc sintaxa de continuare pe linii din
> `cmd.exe` (`^`). În PowerShell, înlocuiește `^` cu backtick (`` ` ``) sau scrie
> comanda pe o singură linie.

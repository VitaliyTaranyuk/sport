Публикация на GitHub Pages

1) Размещение: GitHub Pages может раздавать статический сайт из папки `docs/` на ветке `main`.

2) Включение GitHub Pages:
   - Откройте репозиторий на GitHub
   - Settings → Pages
   - Source: выберите `main` branch и папку `/docs`
   - Сохраните — GitHub сгенерирует ссылку вида `https://<username>.github.io/<repo>/`

3) После включения Pages, сайт станет доступен по ссылке через несколько минут.

4) Локально: можно протестировать, открыв `docs/index.html` в браузере (лучше через локальный HTTP сервер):

```bash
# Python 3
python -m http.server 8000 --directory docs
# или
npx http-server docs -p 8000
```

5) Примечание безопасности: страница запрашивает доступ к камере — разрешите в браузере (лучше в Chrome/Edge/Firefox).

6) Ограничения: Web-версия использует TF.js MoveNet и работает на современных браузерах с WebGL.

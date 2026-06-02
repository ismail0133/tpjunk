#!/usr/bin/env bash
set -euo pipefail

PORT="${JENKINS_PORT:-8080}"
STATE_DIR=".ngrok"
PID_FILE="$STATE_DIR/ngrok.pid"
URL_FILE="$STATE_DIR/url.txt"
LOG_FILE="$STATE_DIR/ngrok.log"

mkdir -p "$STATE_DIR"

if ! command -v ngrok >/dev/null 2>&1; then
    echo "ngrok est introuvable. Lance d'abord : ./scripts/install-ngrok.sh"
    exit 1
fi

ngrok config check >/dev/null

HTTP_STATUS="$(curl -sS -o /dev/null -w '%{http_code}' --max-time 3 "http://127.0.0.1:${PORT}" 2>/dev/null || true)"

if [[ "$HTTP_STATUS" == "000" ]]; then
    echo "Attention : Jenkins ne repond pas encore sur http://localhost:${PORT}"
    echo "Le tunnel sera lance quand meme, mais demarre Jenkins avant de tester le webhook."
fi

if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" >/dev/null 2>&1; then
    echo "Tunnel ngrok deja lance avec le PID $(cat "$PID_FILE")."
else
    echo "Demarrage du tunnel ngrok vers Jenkins : http://localhost:${PORT}"
    nohup ngrok http "$PORT" --log=stdout >"$LOG_FILE" 2>&1 &
    echo "$!" >"$PID_FILE"
fi

PUBLIC_URL=""
for _ in $(seq 1 30); do
    PUBLIC_URL="$(
        curl -fsS http://127.0.0.1:4040/api/tunnels 2>/dev/null \
            | jq -r '.tunnels[] | select(.proto == "https") | .public_url' \
            | head -n 1
    )" || true

    if [[ -n "$PUBLIC_URL" && "$PUBLIC_URL" != "null" ]]; then
        break
    fi
    sleep 1
done

if [[ -z "$PUBLIC_URL" || "$PUBLIC_URL" == "null" ]]; then
    echo "Impossible de recuperer l'URL publique ngrok."
    if grep -q 'ERR_NGROK_4018' "$LOG_FILE" 2>/dev/null; then
        echo "ngrok demande un compte verifie et un authtoken."
        echo "Configure le token puis relance le tunnel :"
        echo "NGROK_AUTHTOKEN=ton_token ./scripts/install-ngrok.sh"
        echo "./scripts/start-ngrok-jenkins.sh"
    fi
    if [[ -f "$PID_FILE" ]] && ! kill -0 "$(cat "$PID_FILE")" >/dev/null 2>&1; then
        rm -f "$PID_FILE"
    fi
    echo "Consulte les logs : $LOG_FILE"
    exit 1
fi

echo "$PUBLIC_URL" >"$URL_FILE"

echo "URL Jenkins publique : $PUBLIC_URL"
echo "Webhook GitHub       : ${PUBLIC_URL}/github-webhook/"
echo "URL sauvegardee dans : $URL_FILE"

#!/usr/bin/env bash
set -euo pipefail

WEBHOOK_URL="${1:-}"

if [[ -z "$WEBHOOK_URL" ]]; then
    if [[ -f ".ngrok/url.txt" ]]; then
        WEBHOOK_URL="$(cat .ngrok/url.txt)/github-webhook/"
    else
        echo "Aucune URL ngrok trouvee."
        echo "Lance d'abord : ./scripts/start-ngrok-jenkins.sh"
        exit 1
    fi
fi

WEBHOOK_URL="${WEBHOOK_URL%/}/"

REMOTE_URL="$(git remote get-url origin)"
REPO_SLUG="$(
    printf '%s' "$REMOTE_URL" \
        | sed -E 's#^git@github.com:##; s#^https://github.com/##; s#\.git$##'
)"

if [[ -z "$REPO_SLUG" || "$REPO_SLUG" != */* ]]; then
    echo "Depot GitHub introuvable depuis origin : $REMOTE_URL"
    exit 1
fi

if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
    echo "Creation du webhook GitHub pour $REPO_SLUG..."
    gh api \
        --method POST \
        "/repos/${REPO_SLUG}/hooks" \
        -f name='web' \
        -F active=true \
        -f 'events[]=push' \
        -f "config[url]=${WEBHOOK_URL}" \
        -f 'config[content_type]=json' \
        -f 'config[insecure_ssl]=0'
    echo "Webhook GitHub cree : $WEBHOOK_URL"
else
    echo "GitHub CLI n'est pas installe ou pas connecte."
    echo "Configuration manuelle :"
    echo "1. Ouvre https://github.com/${REPO_SLUG}/settings/hooks"
    echo "2. Add webhook"
    echo "3. Payload URL : $WEBHOOK_URL"
    echo "4. Content type : application/json"
    echo "5. Events : Just the push event"
    echo "6. Active : coche"
fi

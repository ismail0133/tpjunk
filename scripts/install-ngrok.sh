#!/usr/bin/env bash
set -euo pipefail

if command -v ngrok >/dev/null 2>&1; then
    echo "ngrok est deja installe : $(ngrok version)"
else
    if ! command -v brew >/dev/null 2>&1; then
        echo "Homebrew est requis pour installer ngrok automatiquement sur macOS."
        echo "Installe Homebrew ou installe ngrok depuis https://ngrok.com/download"
        exit 1
    fi

    echo "Installation de ngrok avec Homebrew..."
    brew install ngrok/ngrok/ngrok
fi

AUTHTOKEN="${1:-${NGROK_AUTHTOKEN:-}}"

if [[ -n "$AUTHTOKEN" ]]; then
    echo "Configuration du token ngrok..."
    ngrok config add-authtoken "$AUTHTOKEN"
else
    echo "Aucun token fourni. Si ngrok refuse le tunnel, configure-le avec :"
    echo "NGROK_AUTHTOKEN=ton_token ./scripts/install-ngrok.sh"
fi

ngrok config check
echo "ngrok est pret pour Jenkins."

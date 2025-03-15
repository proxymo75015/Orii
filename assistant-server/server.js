require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { OpenAI } = require('openai');

const app = express();
app.use(cors());
app.use(express.json());

if (!process.env.OPENAI_API_KEY) {
    console.error("ERREUR : La cle OpenAI n'est pas definie ! Verifie les variables d'environnement.");
    process.exit(1);
}

const openai = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });

app.post('/chat-command', async (req, res) => {
    const { message } = req.body;

    console.log("Message recu :", message);

    try {
        const response = await openai.chat.completions.create({
            model: "gpt-4o",
            messages: [{ role: "user", content: message }]
        });

        console.log("Reponse OpenAI :", JSON.stringify(response, null, 2));

        res.json({
            type: "text",
            message: response.choices[0]?.message?.content || "Je ne sais pas quoi repondre."
        });
    } catch (error) {
        console.error("Erreur OpenAI :", error);
        res.status(500).json({ error: "Erreur OpenAI. Verifie ta cle API et ton quota." });
    }
});

// Endpoint de test pour verifier si le serveur tourne
app.get('/healthz', (req, res) => {
    res.status(200).send('OK');
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Serveur lance sur le port ${PORT}`));

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }

  const apiKey = process.env.OPENAI_API_KEY;
  if (!apiKey) {
    return res.status(500).json({ error: "Polly's brain is not configured yet." });
  }

  const message = typeof req.body?.message === "string" ? req.body.message.trim() : "";
  if (!message) {
    return res.status(400).json({ error: "Please give Polly a message." });
  }

  try {
    const response = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${apiKey}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        model: "gpt-5-mini",
        instructions: "You are Polly, a smart, ambitious, hard-working AI assistant from Box Signal. You are warm, calm, organised, capable and practical. Speak naturally as Polly. Help the user get things done. Be concise by default, but give detail when useful. Never claim to have completed actions you cannot actually perform.",
        input: message
      })
    });

    const data = await response.json();

    if (!response.ok) {
      console.error("OpenAI error", data);
      return res.status(502).json({ error: "Polly couldn't reach her AI brain." });
    }

    const reply =
      data.output_text ||
      data.output?.flatMap(item => item.content || [])
        ?.find(part => part.type === "output_text")?.text ||
      "I'm here, but I couldn't form a reply.";

    return res.status(200).json({ reply });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: "Polly had trouble answering." });
  }
}

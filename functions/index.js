const functions = require("firebase-functions");
const {google} = require("googleapis");
const admin = require("firebase-admin");
const path = require("path");
const serviceAccount = require(path.join(__dirname, "credentials.json"));

// Inicializa la app de Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const androidpublisher = google.androidpublisher("v3");

// Cloud Function para obtener los detalles de una suscripcion
exports.getSubscriptionDetails = functions.https.onRequest(async (req, res) => {
  const subscriptionId = req.query.subscriptionId;
  const purchaseToken = req.query.purchaseToken;
  console.log("entro A");
  try {
    const auth = new google.auth.GoogleAuth({
      keyFile: path.join(__dirname, "credentials.json"),
      scopes: ["https://www.googleapis.com/auth/androidpublisher"],
    });
    const authClient = await auth.getClient();
    console.log("entro b");
    const response = await androidpublisher.purchases.subscriptions.get({
      packageName: "com.jmanuel.mikroficha",
      subscriptionId: subscriptionId,
      token: purchaseToken,
      auth: authClient,
    });
    console.log("entro c"+response);

    res.status(200).send(response.data);
  } catch (error) {
    console.error("Error fetching subscription details:", error);
    res.status(500).send("Error fetching subscription 500 "+error);
  }
});

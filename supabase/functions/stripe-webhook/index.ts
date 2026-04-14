import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import Stripe from "https://esm.sh/stripe@13.3.0?target=deno";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const stripe = new Stripe(Deno.env.get("STRIPE_SECRET_KEY") ?? "", {
  apiVersion: "2023-10-16",
  httpClient: Stripe.createFetchHttpClient(),
});

const webhookSecret = Deno.env.get("STRIPE_WEBHOOK_SECRET") ?? "";
const PRICE_MONTHLY = Deno.env.get("STRIPE_PRICE_MONTHLY") ?? "";
const PRICE_YEARLY = Deno.env.get("STRIPE_PRICE_YEARLY") ?? "";

function planFromPriceId(priceId: string): "pro_monthly" | "pro_yearly" | null {
  if (PRICE_YEARLY && priceId === PRICE_YEARLY) return "pro_yearly";
  if (PRICE_MONTHLY && priceId === PRICE_MONTHLY) return "pro_monthly";
  return null;
}

serve(async (req) => {
  const body = await req.text();
  const signature = req.headers.get("stripe-signature") ?? "";

  let event: Stripe.Event;
  try {
    event = await stripe.webhooks.constructEventAsync(body, signature, webhookSecret);
  } catch (err) {
    console.error("Webhook signature verification failed:", err);
    return new Response(JSON.stringify({ error: "Invalid signature" }), { status: 400 });
  }

  const supabase = createClient(
    Deno.env.get("SUPABASE_URL") ?? "",
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""
  );

  try {
    switch (event.type) {
      case "checkout.session.completed": {
        const session = event.data.object as Stripe.Checkout.Session;
        const userId = session.metadata?.supabase_user_id;
        if (!userId) break;

        const subscription = await stripe.subscriptions.retrieve(
          session.subscription as string
        );
        const priceId = subscription.items.data[0]?.price.id ?? "";
        const plan = planFromPriceId(priceId) ?? "pro_monthly";
        const expiresAt = new Date(subscription.current_period_end * 1000).toISOString();

        await supabase.from("profiles").update({
          subscription_plan: plan,
          subscription_expiry: expiresAt,
          stripe_customer_id: session.customer as string,
        }).eq("id", userId);

        await supabase.from("subscriptions").upsert({
          user_id: userId,
          stripe_subscription_id: subscription.id,
          stripe_customer_id: session.customer as string,
          plan,
          status: subscription.status,
          current_period_start: new Date(subscription.current_period_start * 1000).toISOString(),
          current_period_end: expiresAt,
          cancel_at_period_end: subscription.cancel_at_period_end,
        });

        console.log(`Subscription activated for user ${userId}: ${plan}`);
        break;
      }

      case "customer.subscription.updated": {
        const subscription = event.data.object as Stripe.Subscription;
        const customerId = subscription.customer as string;
        const resolvedUserId = await resolveUserIdByCustomer(supabase, customerId);
        if (!resolvedUserId) break;

        const priceId = subscription.items.data[0]?.price.id ?? "";
        const isActive = subscription.status === "active" || subscription.status === "trialing";
        const plan = isActive ? (planFromPriceId(priceId) ?? "pro_monthly") : "free";
        const expiresAt = new Date(subscription.current_period_end * 1000).toISOString();

        await supabase.from("profiles").update({
          subscription_plan: plan,
          subscription_expiry: isActive ? expiresAt : null,
        }).eq("id", resolvedUserId);

        await supabase.from("subscriptions").update({
          plan,
          status: subscription.status,
          current_period_start: new Date(subscription.current_period_start * 1000).toISOString(),
          current_period_end: expiresAt,
          cancel_at_period_end: subscription.cancel_at_period_end,
          updated_at: new Date().toISOString(),
        }).eq("stripe_subscription_id", subscription.id);

        console.log(`Subscription updated for user ${resolvedUserId}: ${plan} (${subscription.status})`);
        break;
      }

      case "customer.subscription.deleted": {
        const subscription = event.data.object as Stripe.Subscription;
        const customerId = subscription.customer as string;
        const resolvedUserId = await resolveUserIdByCustomer(supabase, customerId);
        if (!resolvedUserId) break;

        await supabase.from("profiles").update({
          subscription_plan: "free",
          subscription_expiry: null,
        }).eq("id", resolvedUserId);

        await supabase.from("subscriptions").update({
          plan: "free",
          status: "canceled",
          updated_at: new Date().toISOString(),
        }).eq("stripe_subscription_id", subscription.id);

        console.log(`Subscription canceled for user ${resolvedUserId}`);
        break;
      }

      default:
        console.log(`Unhandled event type: ${event.type}`);
    }
  } catch (err) {
    console.error(`Error processing event ${event.type}:`, err);
    return new Response(JSON.stringify({ error: "Processing error" }), { status: 500 });
  }

  return new Response(JSON.stringify({ received: true }), { status: 200 });
});

async function resolveUserIdByCustomer(
  supabase: ReturnType<typeof createClient>,
  customerId: string
): Promise<string | null> {
  const { data } = await supabase
    .from("profiles")
    .select("id")
    .eq("stripe_customer_id", customerId)
    .single();
  return data?.id ?? null;
}

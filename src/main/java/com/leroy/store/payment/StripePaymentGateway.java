package com.leroy.store.payment;

import com.leroy.store.entities.Order;
import com.leroy.store.entities.OrderItem;
import com.leroy.store.entities.PaymentStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGateway {
    @Value("${frontend-url}")
    private String websiteUrl;

    @Value("${stripe.webhook-secret-key}")
    private String webhookSecretKey;
    @Override
    public CheckoutSession createCheckoutSession(Order order) {
        try {
            var builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(websiteUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(websiteUrl + "/checkout-cancel")
                    .setPaymentIntentData(setPaymentIntentData(order));


            order.getItems()
                    .forEach(item -> {
                        var lineItem = createLineItem(item);
                        builder.addLineItem(lineItem);
                    });

            var session = Session.create(builder.build());

            var sessionUrl = session.getUrl();
            return new CheckoutSession(sessionUrl);

        } catch (StripeException ex){
            System.out.println(ex.getMessage());
            throw new PaymentException();
        }
    }


    @Override
    public Optional<PaymentResult> parseWebhookRequest(WebhookRequest webhookRequest) {
        var payload = webhookRequest.getPayload();
        var signature = webhookRequest.getHeaders().get("Stripe-Signature");
        try {
            var event = Webhook.constructEvent(payload, signature, webhookSecretKey);
            return switch (event.getType()) {
                case "payment_intent.succeeded" ->
                   Optional.of(new PaymentResult(extractOrderId(event), PaymentStatus.PAYED));
                case "payment_intent.payment_failed" ->
                     Optional.of(new PaymentResult(extractOrderId(event), PaymentStatus.FAILED));
                default ->
                    Optional.empty();
            };

        } catch (SignatureVerificationException e) {
            throw new PaymentException("Invalid Stripe-Signature");
        }
    }

    private UUID extractOrderId(Event event){
        var stripeObject =  event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new PaymentException("Could not  deserialize Stripe event. Check the SDK and API version."));
        var paymentIntent = (PaymentIntent) stripeObject;

        return UUID.fromString(paymentIntent.getMetadata().get("order_id"));
    }

    private SessionCreateParams.LineItem createLineItem(OrderItem item) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(Long.valueOf(item.getQuantity()))
                .setPriceData(createPriceData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmountDecimal(item.getUnitPrice().multiply(BigDecimal.valueOf(100)))
                .setProductData(createProductData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData.ProductData createProductData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(item.getProduct().getName())
                .build();
    }

    private static SessionCreateParams.PaymentIntentData setPaymentIntentData(Order order) {
        return SessionCreateParams.PaymentIntentData.builder()
                .putMetadata("order_id", order.getId().toString())
                .build();
    }
}

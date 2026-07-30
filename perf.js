import http from "k6/http";
import { check } from "k6";

export const options = {
  vus: 10,
  iterations: 100,
};

const TOKEN = __ENV.TOKEN;

export default function () {
  const res = http.post(
    "http://localhost:8080/api/v1/trades",
    JSON.stringify({
      tradeRef: `K6-${__VU}-${__ITER}`,
      instrumentSymbol: "SAP.DE",
      counterpartyId: 1,
      quantity: 100,
      price: 245.5,
      tradeDate: "2026-06-02",
    }),
    {
      headers: {
        Authorization: `Bearer ${TOKEN}`,
        "Content-Type": "application/json",
      },
    },
  );
  check(res, { "status is 201": (r) => r.status === 201 });
}

// run: TOKEN=$TOKEN k6 run perf.js

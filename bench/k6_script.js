import http from 'k6/http';
import { sleep } from 'k6';

export let options = { vus: 1, duration: '10s' };

const target = __ENV.TARGET || 'http://localhost';

export default function () {
  http.get(`${target}/`);
  sleep(1);
}

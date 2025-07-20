// MIT License
import http from 'k6/http';
import { sleep } from 'k6';

export let options = { vus: 1, duration: '10s' };

export default function () {
  http.get('http://localhost/');
  sleep(1);
}

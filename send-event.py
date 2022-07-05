#!/usr/local/bin/python3
import requests
import base64
import uuid


token_endpoint = '<your-token-endpoint'
client_id = '<your-client-id>'
client_secret = '<your-client-secret>'
uri = 'https://enterprise-messaging-pubsub.cfapps.sap.hana.ondemand.com'
event_namespace = '/default/sap.fbti.stage/1'
event_type = 'sap.fbti.stage.GoodsMovement.Created.v1'
event_data = {
    "property": "hello"
}


def get_jwt_token(token_endpoint, client_id, client_secret):
    credentials = client_id + ':' + client_secret
    credentials = base64.b64encode(bytes(credentials, 'utf-8')).decode('utf-8')
    headers = {
        'Authorization': 'Basic ' + credentials,
        'Content-Type': 'application/x-www-form-urlencoded'
    }
    form = [
        ('client_id', client_id),
        ('grant_type', 'client_credentials')
    ]
    response = requests.post(token_endpoint, data=form, headers=headers)
    access_token = response.json()["access_token"]
    return access_token


def send_event(uri, access_token, event_namespace, event_type, data):
    event_url = uri + '/sap/ems/v1/events'
    event_id = uuid.uuid4()
    headers = {
        'Authorization': 'Bearer ' + access_token,
        'Content-Type': 'application/cloudevents+json',
        'qos': 'AT_LEAST_ONCE'
    }
    payload = {
        'specversion': '1.0',
        'source': event_namespace,
        'type': event_type,
        'id': str(event_id),
        'data': data
    }
    response = requests.post(event_url, json=payload, headers=headers)
    return response.status_code


if __name__ == '__main__':
    print("Getting JWT token...")
    token = get_jwt_token(token_endpoint, client_id, client_secret)
    print("Token get √")
    print("Sending event...")
    status_code = send_event(uri, token, event_namespace, event_type, event_data)
    if status_code == 204:
        print("Event sent √")
    else:
        print("Failed to send event")

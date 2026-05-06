from flask import Flask, request, jsonify
import joblib
import numpy as np
import os

app = Flask(__name__)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "src", "main", "resources", "models") + os.sep
model      = joblib.load(MODEL_PATH + 'house_pricing_svr_model.pkl')
enc_city   = joblib.load(MODEL_PATH + 'encoder_city.pkl')
enc_dist   = joblib.load(MODEL_PATH + 'encoder_district.pkl')
enc_type   = joblib.load(MODEL_PATH + 'encoder_house_type.pkl')
enc_legal  = joblib.load(MODEL_PATH + 'encoder_legal.pkl')
scaler     = joblib.load(MODEL_PATH + 'scaler.pkl')


def safe_encode(encoder, value, default=0):
    """Encode một giá trị string, trả về default nếu không có trong tập train."""
    try:
        return int(encoder.transform([str(value)])[0])
    except ValueError:
        return default


@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json()

    # Lấy dữ liệu từ Java (city/district/houseType/legal là mã số dạng string)
    area       = float(data.get('area', 0))
    bedrooms   = float(data.get('bedrooms', 0))
    floors     = float(data.get('floors', 1))
    city       = str(data.get('city', '0'))
    district   = str(data.get('district', '0'))
    house_type = str(data.get('houseType', '0'))
    legal      = str(data.get('legal', '0'))

    # Encode categorical
    city_enc  = safe_encode(enc_city,  city)
    dist_enc  = safe_encode(enc_dist,  district)
    type_enc  = safe_encode(enc_type,  house_type)
    legal_enc = safe_encode(enc_legal, legal)

    # Tạo vector đặc trưng đúng thứ tự lúc train:
    # [area, bedrooms, floors, house_type, legal, district, city]
    X_raw = np.array([[area, bedrooms, floors, type_enc, legal_enc, dist_enc, city_enc]])

    # Chuẩn hóa rồi dự đoán
    X_scaled = scaler.transform(X_raw)
    log_price = model.predict(X_scaled)[0]

    # Model train trên log(price) -> chuyển về triệu VNĐ
    price_million_vnd = float(np.exp(log_price))

    return jsonify({'gia_du_doan': price_million_vnd})


if __name__ == '__main__':
    app.run(port=5000)
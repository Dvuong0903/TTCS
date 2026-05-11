from flask import Flask, request, jsonify
import joblib
import numpy as np
import os

app = Flask(__name__)

# --- 1. LOAD BỘ NÃO AI (MODEL & ENCODERS) ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
# Đường dẫn tìm đến thư mục chứa các file .pkl
MODEL_PATH = os.path.join(BASE_DIR, "src", "main", "resources", "models") + os.sep

model      = joblib.load(MODEL_PATH + 'house_pricing_svr_model.pkl')
enc_city   = joblib.load(MODEL_PATH + 'encoder_city.pkl')
enc_dist   = joblib.load(MODEL_PATH + 'encoder_district.pkl')
enc_type   = joblib.load(MODEL_PATH + 'encoder_house_type.pkl')
enc_legal  = joblib.load(MODEL_PATH + 'encoder_legal.pkl')
scaler     = joblib.load(MODEL_PATH + 'scaler.pkl')

def safe_encode(encoder, value, default=0):
    """Hàm mã hóa an toàn: Nếu AI không biết chữ này, nó sẽ trả về số 0 thay vì báo lỗi"""
    try:
        return int(encoder.transform([str(value)])[0])
    except ValueError:
        return default

@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json()

    # 1. Lấy dữ liệu thô từ phía Web/Java gửi sang
    area       = float(data.get('area', 0))
    bedrooms   = float(data.get('bedrooms', 0))
    floors     = float(data.get('floors', 1))
    city       = str(data.get('city', '')).strip()
    district   = str(data.get('district', '')).strip()
    house_type = str(data.get('houseType', '')).strip()
    legal      = str(data.get('legal', '')).strip()

    # --- 2. BỘ PHIÊN DỊCH (Sửa lỗi dữ liệu lệch pha giữa Web và AI) ---
    # Dịch Thành phố
    c_val = city.lower()
    if 'hanoi' in c_val or 'hn' in c_val: city = 'HN'
    elif 'hcm' in c_val or 'hồ chí minh' in c_val: city = 'HCM'

    # Dịch Quận (AI cần có chữ "Quận " hoặc "Huyện " ở đầu tên riêng)
    if district and 'Quận' not in district and 'Huyện' not in district:
        district = 'Quận ' + district
    
    # Dịch Pháp lý (Viết hoa đúng chuẩn từ điển AI)
    l_val = legal.lower()
    if 'sổ đỏ' in l_val: legal = 'Sổ đỏ'
    elif 'sổ hồng' in l_val: legal = 'Sổ hồng'
    elif 'đã có sổ' in l_val: legal = 'Đã có sổ'
    elif 'chưa rõ' in l_val: legal = 'Chưa rõ'

    # Dịch Loại nhà (Ép Chung cư về Nhà ngõ vì AI không học mục Chung cư riêng)
    ht_val = house_type.lower()
    if 'chung cư' in ht_val or 'căn hộ' in ht_val:
        house_type = 'Nhà ngõ, hẻm' 
    elif 'mặt phố' in ht_val or 'mặt tiền' in ht_val:
        house_type = 'Nhà mặt phố, mặt tiền'
    elif 'biệt thự' in ht_val:
        house_type = 'Biệt thự, Villa'
    # --------------------------------------------------

    # 3. Mã hóa dữ liệu chữ sang số để AI đọc được
    city_enc  = safe_encode(enc_city,  city)
    dist_enc  = safe_encode(enc_dist,  district)
    type_enc  = safe_encode(enc_type,  house_type)
    legal_enc = safe_encode(enc_legal, legal)

    # 4. Thực hiện dự đoán (Đơn vị kết quả là Tỷ VNĐ)
    X_raw = np.array([[area, bedrooms, floors, type_enc, legal_enc, dist_enc, city_enc]])
    X_scaled = scaler.transform(X_raw)
    predicted_price = model.predict(X_scaled)[0]

    # --- 5. HẬU XỬ LÝ (Sửa lỗi giá ngược và giới hạn giá trị thực tế) ---
    
    # Fix lỗi "Có sổ rẻ hơn không sổ" bằng logic thưởng/phạt
    if legal in ['Sổ đỏ', 'Sổ hồng', 'Đã có sổ']:
        predicted_price *= 1.25 # Thưởng 25% giá trị cho nhà pháp lý sạch
    elif legal == 'Chưa rõ':
        predicted_price *= 0.85 # Phạt 15% giá trị cho nhà rủi ro pháp lý

    # Khống chế giá trị để không ra kết quả "ảo ma"
    if predicted_price < 0.5: predicted_price = 0.5 # Không cho phép nhà rẻ dưới 500 triệu
    if predicted_price > 50: predicted_price = 50   # Giới hạn tối đa 50 tỷ để tránh nhiễu dữ liệu

    # Chuyển về đơn vị Triệu VNĐ cho Web hiển thị (Tỷ * 1000)
    price_million = float(predicted_price * 1000)

    # In kết quả ra màn hình Terminal để sếp dễ kiểm tra
    print(f"\n--- LOG DỰ ĐOÁN ---")
    print(f"Đầu vào: {city}, {district} | Loại: {house_type} | Pháp lý: {legal}")
    print(f"Mã hóa AI: City={city_enc}, Dist={dist_enc}, Type={type_enc}, Legal={legal_enc}")
    print(f"Kết quả: {predicted_price:.2f} Tỷ ({price_million:,.0f} Triệu VNĐ)")

    return jsonify({'gia_du_doan': price_million})

if __name__ == '__main__':
    # Chạy trạm AI ở cổng 5000
    app.run(port=5000, debug=True)
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>QR Code Generator</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        // Function to generate the QR code via AJAX
        function generateQRCode() {
            var data = $('#data').val();
            var error = $('#error').val();
            var bg = $('#bg').val();
            var fg = $('#fg').val();
            var margin = $('#margin').val();
            var size = $('#size').val();

            // Sending AJAX request to the servlet
            $.get('MakeQR', {
                data: data,
                error: error,
                bg: bg,
                fg: fg,
                margin: margin,
                size: size
            }, function(response) {
                // Update the QR code image with the received base64 data
                $('#qrImage').attr('src', 'data:image/png;base64,' + response);
            }).fail(function() {
                alert("Failed to generate QR code. Please check the form inputs.");
            });
        }

        $(document).ready(function() {
            // When the form is submitted, prevent default and trigger AJAX function
            $('form').submit(function(event) {
                event.preventDefault(); // Prevent the form from submitting normally
                generateQRCode(); // Call the function to generate the QR
            });
        });
    </script>
</head>
<body>
    <h2>Generate Static QR Code</h2>
    <form>
        <label for="data">Text to Encode:</label>
        <input type="text" id="data" name="data" value="Hello World"><br><br>

        <label for="error">Error Correction Level (0-3):</label>
        <input type="number" id="error" name="error" min="0" max="3" value="0"><br><br>

        <label for="bg">Background Color (hex):</label>
        <input type="text" id="bg" name="bg" value="FFFFFF"><br><br>

        <label for="fg">Foreground Color (hex):</label>
        <input type="text" id="fg" name="fg" value="000000"><br><br>

        <label for="margin">Margin:</label>
        <input type="number" id="margin" name="margin" min="1" max="50" value="10"><br><br>

        <label for="size">Size:</label>
        <input type="number" id="size" name="size" min="1" max="100" value="100"><br><br>

        <input type="submit" value="Generate QR">
    </form>

    <hr>

    <h3>Generated QR Code:</h3>
    <img id="qrImage" src="" alt="QR Code" style="max-width: 300px;"/>

</body>
</html>

const { useState, useEffect } = require("react");
const { useAuth } = require("../context/AuthContext");
const { useRouter } = require("next/router");

const LoginPage = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [IsSubmitting, setIsSubmitting] = useState(false);

  const { login, isAuthenticated } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isAuthenticated) {
      router.push("/chat");
    }
  }, [isAuthenticated, router]);

  const handleSubmit = async () => {
    e.preventDefault();

    setError("");
    setIsSubmitting(true);

    if (!username || !password) {
      setError("Please enter both username and password.");
      setIsSubmitting(false);
      return;
    }

    const success = await login(username, password);

    if (success) {
      router.push("/push");
    } else {
      setError("Login failed. Please check your credentials.");
    }

    setIsSubmitting(false);
  };

  if (isAuthenticated) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-50">
        <p className="text-xl text-gray-700">Redirecting to chat...</p>
      </div>
    );
  }
};

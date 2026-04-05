import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Lock, Mail, Loader2 } from 'lucide-react';
import { authApi, LoginRequest } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

const schema = z.object({
  email:    z.string().email('Enter a valid email'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type FormData = z.infer<typeof schema>;

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    setLoading(true);
    try {
      const res = await authApi.login(data as LoginRequest);
      login(res.data.accessToken, res.data.user);
      toast.success(`Welcome back, ${res.data.user.firstName}!`);

      const roles = res.data.user.roles;
      if (roles.includes('ADMIN'))    navigate('/admin/dashboard');
      else if (roles.includes('DEALER')) navigate('/dealer/dashboard');
      else navigate('/customer/dashboard');
    } catch (err: any) {
      const msg = err.response?.data?.message ?? 'Invalid credentials';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="auth-logo-icon">S</div>
          <div>
            <div className="auth-logo-text">Serene</div>
          </div>
        </div>

        <h2 className="auth-title">Welcome back</h2>
        <p className="auth-subtitle">Sign in to your Serene DMS account</p>

        <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
          <div className="form-group">
            <label className="form-label">Email address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                {...register('email')}
                type="email"
                placeholder="admin@serene.com"
                className="form-input"
                style={{ paddingLeft: 38 }}
                autoComplete="email"
              />
            </div>
            {errors.email && <span className="form-error">{errors.email.message}</span>}
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              <input
                {...register('password')}
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••"
                className="form-input"
                style={{ paddingLeft: 38, paddingRight: 40 }}
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{ position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', display: 'flex' }}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
            {errors.password && <span className="form-error">{errors.password.message}</span>}
          </div>

          <button type="submit" className="btn btn-primary w-full" disabled={loading}
            style={{ width: '100%', justifyContent: 'center', padding: '13px 20px', fontSize: '0.95rem', marginTop: 6 }}>
            {loading ? <><Loader2 size={17} style={{ animation: 'spin 0.7s linear infinite' }} /> Signing in...</> : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer">
          <span>Default: admin@serene.com / Admin@123</span>
        </div>
      </div>
    </div>
  );
}
